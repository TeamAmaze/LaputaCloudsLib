package com.amaze.laputacloudslib

import com.amaze.laputacloudslib.box.toFile
import com.box.androidsdk.content.BoxApiFile
import com.box.androidsdk.content.BoxApiFolder
import com.box.androidsdk.content.BoxConstants
import com.box.androidsdk.content.models.BoxItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class AbstractFileStructureDriver {
    abstract fun getRoot(): CloudPath

    abstract suspend fun getFiles(path: CloudPath, callback: suspend (List<AbstractCloudFile>) -> Unit)

    abstract suspend fun getFile(path: CloudPath, callback: suspend (AbstractCloudFile) -> Unit)
}

class BoxDriver(
    private val folderApi: BoxApiFolder,
    private val fileApi: BoxApiFile
) : AbstractFileStructureDriver() {
    override fun getRoot(): CloudPath = BoxPath(BoxConstants.ROOT_FOLDER_ID, isDirectory = true, isRoot = true)

    override suspend fun getFiles(
        path: CloudPath,
        callback: suspend (List<AbstractCloudFile>) -> Unit
    ) {
        path as BoxPath

        withContext(Dispatchers.IO) {
            val children = folderApi.getItemsRequest(path.id).send().map(BoxItem::toFile)

            withContext(Dispatchers.Main) {
                callback(children)
            }
        }
    }

    override suspend fun getFile(path: CloudPath, callback: suspend (AbstractCloudFile) -> Unit) {
        path as BoxPath

        withContext(Dispatchers.IO) {
            val fileInfo: BoxItem? = when {
                path.isRoot -> null
                path.isDirectory -> folderApi.getInfoRequest(path.id).send()
                else -> fileApi.getInfoRequest(path.id).send()
            }

            withContext(Dispatchers.Main) {
                callback(fileInfo?.toFile() ?: BoxFile(path, fileInfo))
            }
        }
    }

}
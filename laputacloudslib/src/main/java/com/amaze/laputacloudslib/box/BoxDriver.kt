package com.amaze.laputacloudslib.box

import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.amaze.laputacloudslib.CloudPath
import com.box.androidsdk.content.BoxApiFile
import com.box.androidsdk.content.BoxApiFolder
import com.box.androidsdk.content.BoxConstants
import com.box.androidsdk.content.models.BoxItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BoxDriver(
    private val folderApi: BoxApiFolder,
    private val fileApi: BoxApiFile
) : AbstractFileStructureDriver<BoxPath, BoxFile>() {
    override fun getRoot(): BoxPath = BoxPath(BoxConstants.ROOT_FOLDER_ID, isDirectory = true, isRoot = true)

    override suspend fun getFiles(
        path: BoxPath,
        callback: suspend (List<BoxFile>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val children = folderApi.getItemsRequest(path.id).send().map { info -> info.toFile(fileApi) }

            withContext(Dispatchers.Main) {
                callback(children)
            }
        }
    }

    override suspend fun getFile(path: BoxPath, callback: suspend (BoxFile) -> Unit) {
        withContext(Dispatchers.IO) {
            val fileInfo: BoxItem = when {
                path.isRoot -> TODO("Missing root")
                path.isDirectory -> folderApi.getInfoRequest(path.id).send()
                else -> fileApi.getInfoRequest(path.id).send()
            }

            withContext(Dispatchers.Main) {
                callback(fileInfo.toFile(fileApi))
            }
        }
    }

}
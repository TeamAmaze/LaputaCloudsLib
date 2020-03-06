package com.amaze.laputacloudslib

import com.box.androidsdk.content.BoxApiFile
import com.box.androidsdk.content.BoxApiFolder
import com.box.androidsdk.content.BoxConstants

abstract class AbstractFileStructureDriver {
    abstract fun getRoot(): CloudPath

    abstract suspend fun getFiles(path: CloudPath, callback: suspend (List<AbstractCloudFile>) -> Unit)

    abstract suspend fun getFile(path: CloudPath, callback: suspend (AbstractCloudFile) -> Unit)
}

class BoxDriver(
    private val folderApi: BoxApiFolder,
    private val fileApi: BoxApiFile
) : AbstractFileStructureDriver() {
    override fun getRoot(): CloudPath = BoxPath("/")

    override suspend fun getFiles(
        path: CloudPath,
        callback: suspend (List<AbstractCloudFile>) -> Unit
    ) {

        val rootFolder = folderApi.getItemsRequest(BoxConstants.ROOT_FOLDER_ID)
    }

    override suspend fun getFile(path: CloudPath, callback: suspend (AbstractCloudFile) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
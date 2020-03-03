package com.amaze.laputacloudslib

import com.box.sdk.BoxAPIConnection
import com.box.sdk.BoxFolder

abstract class AbstractFileStructureDriver {
    abstract fun getRoot(): CloudPath

    abstract suspend fun getFiles(path: CloudPath, callback: suspend (List<AbstractCloudFile>) -> Unit)

    abstract suspend fun getFile(path: CloudPath, callback: suspend (AbstractCloudFile) -> Unit)
}

class BoxDriver(private val api: BoxAPIConnection) : AbstractFileStructureDriver() {
    override fun getRoot(): CloudPath = BoxPath("/")

    override suspend fun getFiles(
        path: CloudPath,
        callback: suspend (List<AbstractCloudFile>) -> Unit
    ) {

        val rootFolder = BoxFolder.getRootFolder(api)
    }

    override suspend fun getFile(path: CloudPath, callback: suspend (AbstractCloudFile) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
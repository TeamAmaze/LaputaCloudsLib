package com.amaze.laputacloudslib

import com.amaze.laputacloudslib.AbstractCloudPath.Companion.SEPARATOR
import com.amaze.laputacloudslib.dropbox.isFolder
import com.amaze.laputacloudslib.dropbox.toDropBoxFile
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class AbstractFileStructureDriver {
    abstract fun getRoot(): CloudPath

    abstract suspend fun getFiles(path: CloudPath, callback: suspend (List<AbstractCloudFile>) -> Unit)

    abstract suspend fun getFile(path: CloudPath, callback: suspend (AbstractCloudFile) -> Unit)
}

class DropBoxDriver(val client: DbxClientV2) : AbstractFileStructureDriver() {
    companion object {
        const val SCHEME = "dropbox:"
    }

    override fun getRoot(): CloudPath {
        return DropBoxPath("/")
    }

    override suspend fun getFiles(path: CloudPath, callback: suspend (List<AbstractCloudFile>) -> Unit) {
        path as DropBoxPath

        withContext(Dispatchers.IO) {
            var result = client.files().listFolder(path.sanitizedPathOrRoot)

            val fileList = mutableListOf<AbstractCloudFile>()

            while (true) {
                fileList.addAll(result.entries.map { DropBoxFile(
                    this@DropBoxDriver,
                    DropBoxPath(it.pathLower),
                    false,
                    it.name,
                    it.isFolder()
                ) })

                if (!result.hasMore) {
                    break
                }

                result = client.files().listFolderContinue(result.cursor);
            }

            withContext(Dispatchers.Main) {
                callback(fileList)
            }
        }
    }

    override suspend fun getFile(path: CloudPath, callback: suspend (AbstractCloudFile) -> Unit) {
        path as DropBoxPath

        withContext(Dispatchers.IO) {
            val file = if (path.sanitizedPathOrRoot.isNotEmpty()) {//root folder has no metadata
                val metadata = client.files().getMetadata(path.sanitizedPath)
                metadata.toDropBoxFile(this@DropBoxDriver)
            } else {
                DropBoxFile(
                    this@DropBoxDriver,
                    DropBoxPath("/"),
                    true,
                    "root",
                    true)
            }

            withContext(Dispatchers.Main) {
                callback(file)
            }
        }
    }

}
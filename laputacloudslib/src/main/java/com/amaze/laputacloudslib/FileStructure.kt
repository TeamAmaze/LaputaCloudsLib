package com.amaze.laputacloudslib

import com.amaze.laputacloudslib.CloudPath.Companion.SEPARATOR
import com.amaze.laputacloudslib.CloudPath.Companion.crashyCheckAgainst
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
        crashyCheckAgainst<DropBoxPath>(path)

        withContext(Dispatchers.IO) {
            var result = client.files().listFolder(if(path.sanitizedPath == SEPARATOR) "" else path.sanitizedPath)

            val fileList = mutableListOf<AbstractCloudFile>()

            while (true) {
                fileList.addAll(result.entries.map { DropBoxFile(
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
        crashyCheckAgainst<DropBoxPath>(path)

        withContext(Dispatchers.IO) {
            val name: String
            val rawPath: String
            val isDirectory: Boolean

            if (path.sanitizedPath != SEPARATOR) {//root folder has no metadata
                val metadata = client.files().getMetadata(path.sanitizedPath)
                name = metadata.name
                rawPath = metadata.pathLower
                isDirectory = metadata.isFolder()
            } else {
                name = "root"
                rawPath = "/"
                isDirectory = true
            }

            withContext(Dispatchers.Main) {
                callback(DropBoxFile(
                    DropBoxPath(rawPath),
                    rawPath == SEPARATOR,
                    name,
                    isDirectory))
            }
        }
    }

    private fun Metadata.isFolder(): Boolean {
        return this is FolderMetadata //as per https://www.dropboxforum.com/t5/API-Support-Feedback/Finding-if-Metadata-is-for-file-or-folder/td-p/167606
    }


}
package com.amaze.laputacloudslib

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.files.FolderMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

abstract class AbstractFileStructureDriver {
    companion object {
        val SEPARATOR = "/"

        @VisibleForTesting
        fun removeScheme(path: String, scheme: String) = path.substringAfter(scheme)

        @VisibleForTesting(otherwise = PROTECTED)
        fun sanitizeRawPath(rawPath: String): String {
            var rawPath = rawPath

            if(!rawPath.startsWith(SEPARATOR)) {
                rawPath = SEPARATOR + rawPath
            }

            return File(rawPath).canonicalPath
        }
    }

    abstract val SCHEME: String

    abstract suspend fun getFiles(path: String, callback: suspend (List<AbstractCloudFile>) -> Unit)

    abstract suspend fun getFile(path: String, callback: suspend (AbstractCloudFile) -> Unit)

    protected fun removeScheme(path: String) = removeScheme(path, SCHEME)
}

class DropBoxDriver(val client: DbxClientV2) : AbstractFileStructureDriver() {
    companion object {
        const val SCHEME = "dropbox:"
    }

    override val SCHEME: String =
        Companion.SCHEME

    override suspend fun getFiles(path: String, callback: suspend (List<AbstractCloudFile>) -> Unit) {
        withContext(Dispatchers.IO) {
            val path = sanitizeRawPath(removeScheme(path))
            var result = client.files().listFolder(if(path == SEPARATOR) "" else path)

            val fileList = mutableListOf<AbstractCloudFile>()

            while (true) {
                fileList.addAll(result.entries.map { DropBoxFile(
                    SCHEME + it.pathLower,
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

    override suspend fun getFile(path: String, callback: suspend (AbstractCloudFile) -> Unit) {
        withContext(Dispatchers.IO) {
            val name: String
            val schemedPath: String
            val isDirectory: Boolean

            if (sanitizeRawPath(removeScheme(path)) != SEPARATOR) {//root folder has no metadata
                val metadata = client.files().getMetadata(sanitizeRawPath(removeScheme(path)))
                name = metadata.name
                schemedPath = SCHEME + metadata.pathLower
                isDirectory = metadata.isFolder()
            } else {
                name = "root"
                schemedPath = "$SCHEME/"
                isDirectory = true
            }

            withContext(Dispatchers.Main) {
                callback(DropBoxFile(
                    schemedPath,
                    removeScheme(schemedPath) == SEPARATOR,
                    name,
                    isDirectory))
            }
        }
    }

    private fun Metadata.isFolder(): Boolean {
        return this is FolderMetadata //as per https://www.dropboxforum.com/t5/API-Support-Feedback/Finding-if-Metadata-is-for-file-or-folder/td-p/167606
    }


}
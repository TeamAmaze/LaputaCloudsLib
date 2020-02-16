package com.amaze.laputacloudslib

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
import com.dropbox.core.v2.DbxClientV2
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

    abstract fun getFiles(path: String, callback: (List<AbstractCloudFile>) -> Unit)

    abstract fun getFile(path: String, callback: (AbstractCloudFile) -> Unit)

    protected fun removeScheme(path: String) = removeScheme(path, SCHEME)
}

class DropBoxDriver(val client: DbxClientV2) : AbstractFileStructureDriver() {
    companion object {
        const val SCHEME = "dropbox:"
    }

    override val SCHEME: String =
        Companion.SCHEME

    override fun getFiles(path: String, callback: (List<AbstractCloudFile>) -> Unit) {
        // Get files and folder metadata from Dropbox root directory
        var result = client.files().listFolder(sanitizeRawPath(removeScheme(path)))

        val fileList = mutableListOf<AbstractCloudFile>()

        while (true) {
            fileList.addAll(result.entries.map { DropBoxFile() })

            if (!result.hasMore) {
                break
            }

            result = client.files().listFolderContinue(result.cursor);
        }

        callback(fileList)
    }

    override fun getFile(path: String, callback: (AbstractCloudFile) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
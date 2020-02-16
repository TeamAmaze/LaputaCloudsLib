package com.amaze.laputacloudslib

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
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


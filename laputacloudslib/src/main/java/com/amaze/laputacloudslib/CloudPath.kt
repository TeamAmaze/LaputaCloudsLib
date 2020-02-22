package com.amaze.laputacloudslib

import androidx.annotation.VisibleForTesting
import com.amaze.laputacloudslib.onedrive.OneDriveDriver
import java.io.File

abstract class CloudPath(path: String) {
    companion object {
        @JvmStatic
        inline fun <reified T : CloudPath> crashyCheckAgainst(cloudPath: CloudPath) {
            cloudPath as? T ?: throw IllegalArgumentException("Wrong path!")
        }

        val SEPARATOR = "/"

        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
        fun sanitizeRawPath(rawPath: String): String {
            var rawPath = rawPath

            if (!rawPath.startsWith(SEPARATOR)) {
                rawPath = SEPARATOR + rawPath
            }

            return File(rawPath).canonicalPath
        }
    }

    abstract val scheme: String

    val sanitizedPath = sanitizeRawPath(path)

    val fullPath: String
            get() = scheme + sanitizedPath

    inline fun <reified T: CloudPath> getParentFromPath(): T
            = createInstanceOfSubclass(File(sanitizedPath).parent!!) as T

    inline fun <reified T: CloudPath> join(fileName: String): T
            = createInstanceOfSubclass(sanitizedPath + SEPARATOR + fileName) as T

    abstract fun createInstanceOfSubclass(path: String): CloudPath
}

class OneDrivePath(path: String) : CloudPath(path) {
    override val scheme: String = OneDriveDriver.SCHEME

    override fun createInstanceOfSubclass(path: String): CloudPath {
        return OneDrivePath(path)
    }
}

class DropBoxPath(path: String) : CloudPath(path) {
    override val scheme: String = DropBoxDriver.SCHEME

    override fun createInstanceOfSubclass(path: String): CloudPath {
        return DropBoxPath(path)
    }
}
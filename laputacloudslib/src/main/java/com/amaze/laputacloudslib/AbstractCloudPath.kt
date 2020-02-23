package com.amaze.laputacloudslib

import androidx.annotation.VisibleForTesting
import com.amaze.laputacloudslib.onedrive.OneDriveDriver
import java.io.File

interface CloudPath {
    val scheme: String

    val sanitizedPath: String

    val fullPath: String

    fun getParentFromPath(): CloudPath

    fun join(fileName: String): CloudPath
}

abstract class AbstractCloudPath<Path: AbstractCloudPath<Path>>(path: String): CloudPath {
    companion object {
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

    abstract override val scheme: String

    override val sanitizedPath = sanitizeRawPath(path)

    override val fullPath: String
            get() = scheme + sanitizedPath

    override fun getParentFromPath(): Path
            = createInstanceOfSubclass(File(sanitizedPath).parent!!)

    override fun join(fileName: String): Path
            = createInstanceOfSubclass(sanitizedPath + SEPARATOR + fileName)

    abstract fun createInstanceOfSubclass(path: String): Path
}

class OneDrivePath(path: String) : AbstractCloudPath<OneDrivePath>(path) {
    override val scheme: String = OneDriveDriver.SCHEME

    override fun createInstanceOfSubclass(path: String): OneDrivePath {
        return OneDrivePath(path)
    }
}

class DropBoxPath(path: String) : AbstractCloudPath<DropBoxPath>(path) {
    override val scheme: String = DropBoxDriver.SCHEME

    override fun createInstanceOfSubclass(path: String): DropBoxPath {
        return DropBoxPath(path)
    }
}
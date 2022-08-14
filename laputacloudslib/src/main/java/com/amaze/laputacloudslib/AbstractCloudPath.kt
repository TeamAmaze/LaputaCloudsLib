package com.amaze.laputacloudslib

import androidx.annotation.VisibleForTesting
import java.io.File

interface CloudPath {
    val scheme: String

    val sanitizedPath: String

    val fullPath: String
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
}
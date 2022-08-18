package com.amaze.laputacloudslib

import java.io.InputStream

abstract class AbstractCloudFile<Path: CloudPath, File: AbstractCloudFile<Path, File>> {
    abstract val name: String
    abstract val path: Path
    abstract val isDirectory: Boolean
    abstract val isRootDirectory: Boolean
    abstract val byteSize: Long

    abstract fun getParent(callback: suspend (File?) -> Unit)
    abstract fun delete(callback: () -> Unit)
    abstract fun copyTo(newName: String, folder: File, callback: (File) -> Unit)
    abstract fun moveTo(newName: String, folder: File, callback: (File) -> Unit)

    /**
     * You need to close the [InputStream] when not using it anymore
     */
    abstract fun download(callback: (InputStream) -> Unit)

    /**
     * There is no guarantee that the [onProgress] function will be called
     */
    abstract fun uploadHere(fileToUpload: File, onProgress: ((bytes: Long) -> Unit)?, callback: (uploadedFile: File) -> Unit)

    /**
     * There is no guarantee that the [onProgress] function will be called
     */
    abstract fun uploadHere(inputStream: InputStream, name: String, size: Long, onProgress: ((bytes: Long) -> Unit)?, callback: (uploadedFile: File) -> Unit)
}

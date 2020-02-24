package com.amaze.laputacloudslib

import java.io.InputStream


abstract class AbstractCloudFile {
    abstract val name: String
    abstract val path: CloudPath
    abstract val isDirectory: Boolean
    abstract val isRootDirectory: Boolean
    abstract val byteSize: Long

    abstract fun getParent(callback: suspend (AbstractCloudFile?) -> Unit)
    abstract fun delete(callback: () -> Unit)
    abstract fun copyTo(newName: String, folder: AbstractCloudFile, callback: (AbstractCloudFile) -> Unit)
    abstract fun moveTo(newName: String, folder: AbstractCloudFile, callback: (AbstractCloudFile) -> Unit)

    /**
     * You need to close the [InputStream] when not using it anymore
     */
    abstract fun download(callback: (InputStream) -> Unit)

    /**
     * There is no guarantee that the [onProgress] function will be called
     */
    abstract fun uploadHere(fileToUpload: AbstractCloudFile, onProgress: ((bytes: Long) -> Unit)?, callback: (uploadedFile: AbstractCloudFile) -> Unit)

    /**
     * There is no guarantee that the [onProgress] function will be called
     */
    abstract fun uploadHere(inputStream: InputStream, name: String, size: Long, onProgress: ((bytes: Long) -> Unit)?, callback: (uploadedFile: AbstractCloudFile) -> Unit)
}


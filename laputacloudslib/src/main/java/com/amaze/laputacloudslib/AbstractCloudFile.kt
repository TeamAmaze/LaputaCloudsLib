package com.amaze.laputacloudslib

import arrow.core.Either
import java.io.InputStream

abstract class AbstractCloudFile<Path: CloudPath, File: AbstractCloudFile<Path, File>> {
    abstract val name: String
    abstract val path: Path
    abstract val isDirectory: Boolean
    abstract val isRootDirectory: Boolean
    abstract val byteSize: Long

    abstract fun getParent(callback: suspend (Either<Exception, File>) -> Unit)
    abstract fun delete(callback: (Either<Exception, Unit>) -> Unit)
    abstract fun copyTo(newName: String, folder: File, callback: (Either<Exception, File>) -> Unit)
    abstract fun moveTo(newName: String, folder: File, callback: (Either<Exception, File>) -> Unit)

    /**
     * You need to close the [InputStream] when not using it anymore
     */
    abstract fun download(callback: (Either<Exception, InputStream>) -> Unit)

    /**
     * There is no guarantee that the [onProgress] function will be called
     */
    abstract fun uploadHere(fileToUpload: File, onProgress: ((bytes: Long) -> Unit)?, callback: (uploadedFile: Either<Exception, File>) -> Unit)

    /**
     * There is no guarantee that the [onProgress] function will be called
     */
    abstract fun uploadHere(inputStream: InputStream, name: String, size: Long, onProgress: ((bytes: Long) -> Unit)?, callback: (uploadedFile: Either<Exception, File>) -> Unit)
}

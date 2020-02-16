package com.amaze.laputacloudslib

import java.io.InputStream

abstract class AbstractCloudFile {
    abstract val name: String
    abstract val path: String
    abstract val isDirectory: Boolean
    abstract val isRootDirectory: Boolean
    abstract val byteSize: Long

    abstract fun getParent(callback: (AbstractCloudFile?) -> Unit)
    abstract fun delete(callback: () -> Unit)
    abstract fun copyTo(newName: String, folder: AbstractCloudFile, callback: (AbstractCloudCopyStatus) -> Unit)
    abstract fun moveTo(newName: String, folder: AbstractCloudFile, callback: (AbstractCloudFile) -> Unit)
    abstract fun download(callback: (InputStream) -> Unit)
    abstract fun uploadHere(fileToUpload: AbstractCloudFile, callback: (uploadedFile: AbstractCloudFile) -> Unit)
    abstract fun uploadHere(inputStream: InputStream, name: String, size: Long, callback: (uploadedFile: AbstractCloudFile) -> Unit)
}


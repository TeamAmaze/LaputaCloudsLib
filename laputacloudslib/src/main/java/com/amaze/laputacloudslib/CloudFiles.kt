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
    abstract fun copyTo(newName: String, folder: AbstractCloudFile, callback: (AbstractCloudCopyStatus) -> Unit)
    abstract fun moveTo(newName: String, folder: AbstractCloudFile, callback: (AbstractCloudFile) -> Unit)
    abstract fun download(callback: (InputStream) -> Unit)
    abstract fun uploadHere(fileToUpload: AbstractCloudFile, callback: (uploadedFile: AbstractCloudFile) -> Unit)
    abstract fun uploadHere(inputStream: InputStream, name: String, size: Long, callback: (uploadedFile: AbstractCloudFile) -> Unit)
}

class DropBoxFile(
    override val path: DropBoxPath,
    override val isRootDirectory: Boolean,
    override val name: String,
    override val isDirectory: Boolean
) : AbstractCloudFile() {
    override val byteSize: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun getParent(callback: suspend (AbstractCloudFile?) -> Unit) {
    }

    override fun delete(callback: () -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun copyTo(
        newName: String,
        folder: AbstractCloudFile,
        callback: (AbstractCloudCopyStatus) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun moveTo(
        newName: String,
        folder: AbstractCloudFile,
        callback: (AbstractCloudFile) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun download(callback: (InputStream) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun uploadHere(
        fileToUpload: AbstractCloudFile,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
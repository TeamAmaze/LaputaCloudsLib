package com.amaze.laputacloudslib

import com.box.androidsdk.content.models.BoxItem
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

class BoxFile(
    override val path: BoxPath,
    private val info: BoxItem?
): AbstractCloudFile() {
    override val name: String = info?.name ?: "root"
    override val isDirectory: Boolean = path.isDirectory
    override val isRootDirectory: Boolean = path.isRoot
    override val byteSize: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun getParent(callback: suspend (AbstractCloudFile?) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(callback: () -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun copyTo(
        newName: String,
        folder: AbstractCloudFile,
        callback: (AbstractCloudFile) -> Unit
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
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
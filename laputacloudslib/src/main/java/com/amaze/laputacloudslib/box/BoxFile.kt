package com.amaze.laputacloudslib.box

import com.amaze.laputacloudslib.AbstractCloudFile
import com.box.androidsdk.content.models.BoxItem
import java.io.InputStream

class BoxFile(
    override val path: BoxPath,
    private val info: BoxItem
): AbstractCloudFile() {
    override val name: String = info.name ?: "root"
    override val isDirectory: Boolean = path.isDirectory
    override val isRootDirectory: Boolean = path.isRoot
    override val byteSize: Long
        get() = info.size

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
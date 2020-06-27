package com.amaze.laputacloudslib.googledrive

import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.CloudPath
import java.io.InputStream

class GoogleDriveFile(
    override val name: String,
    override val path: CloudPath
) : AbstractCloudFile() {
    override val isDirectory: Boolean
        get() = TODO("Not yet implemented")
    override val isRootDirectory: Boolean
        get() = TODO("Not yet implemented")
    override val byteSize: Long
        get() = TODO("Not yet implemented")

    override fun getParent(callback: suspend (AbstractCloudFile?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun delete(callback: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun copyTo(
        newName: String,
        folder: AbstractCloudFile,
        callback: (AbstractCloudFile) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun moveTo(
        newName: String,
        folder: AbstractCloudFile,
        callback: (AbstractCloudFile) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun download(callback: (InputStream) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun uploadHere(
        fileToUpload: AbstractCloudFile,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        TODO("Not yet implemented")
    }

}
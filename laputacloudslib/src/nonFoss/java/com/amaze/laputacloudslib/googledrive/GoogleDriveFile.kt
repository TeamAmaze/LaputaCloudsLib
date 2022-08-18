package com.amaze.laputacloudslib.googledrive

import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.CloudPath
import java.io.InputStream

class GoogleDriveFile(
    override val name: String,
    override val path: GoogleDrivePath
) : AbstractCloudFile<GoogleDrivePath, GoogleDriveFile>() {
    override val isDirectory: Boolean
        get() = TODO("Not yet implemented")
    override val isRootDirectory: Boolean
        get() = TODO("Not yet implemented")
    override val byteSize: Long
        get() = TODO("Not yet implemented")

    override fun getParent(callback: suspend (GoogleDriveFile?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun delete(callback: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun copyTo(
        newName: String,
        folder: GoogleDriveFile,
        callback: (GoogleDriveFile) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun moveTo(
        newName: String,
        folder: GoogleDriveFile,
        callback: (GoogleDriveFile) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun download(callback: (InputStream) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun uploadHere(
        fileToUpload: GoogleDriveFile,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: GoogleDriveFile) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: GoogleDriveFile) -> Unit
    ) {
        TODO("Not yet implemented")
    }

}
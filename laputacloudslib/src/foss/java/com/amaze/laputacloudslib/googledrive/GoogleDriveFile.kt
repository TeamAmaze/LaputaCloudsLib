package com.amaze.laputacloudslib.googledrive

import arrow.core.Either
import com.amaze.laputacloudslib.AbstractCloudFile
import java.io.InputStream

class GoogleDriveFile: AbstractCloudFile<GoogleDrivePath, GoogleDriveFile>() {
    override val name: String
        get() = TODO("Not yet implemented")
    override val path: GoogleDrivePath
        get() = TODO("Not yet implemented")
    override val isDirectory: Boolean
        get() = TODO("Not yet implemented")
    override val isRootDirectory: Boolean
        get() = TODO("Not yet implemented")
    override val byteSize: Long
        get() = TODO("Not yet implemented")

    override fun getParent(callback: suspend (Either<Exception, GoogleDriveFile>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun delete(callback: (Either<Exception, Unit>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun copyTo(
        newName: String,
        folder: GoogleDriveFile,
        callback: (Either<Exception, GoogleDriveFile>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun moveTo(
        newName: String,
        folder: GoogleDriveFile,
        callback: (Either<Exception, GoogleDriveFile>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun download(callback: (Either<Exception, InputStream>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun uploadHere(
        fileToUpload: GoogleDriveFile,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: Either<Exception, GoogleDriveFile>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: Either<Exception, GoogleDriveFile>) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}
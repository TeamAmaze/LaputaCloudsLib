package com.amaze.laputacloudslib.googledrive

import arrow.core.Either
import com.amaze.laputacloudslib.AbstractFileStructureDriver

class GoogleDriveDriver: AbstractFileStructureDriver<GoogleDrivePath, GoogleDriveFile>() {
    override fun getRoot(): GoogleDrivePath {
        TODO("Not yet implemented")
    }

    override suspend fun getFiles(
        path: GoogleDrivePath,
        callback: suspend (Either<Exception, List<GoogleDriveFile>>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getFile(
        path: GoogleDrivePath,
        callback: suspend (Either<Exception, GoogleDriveFile>) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}
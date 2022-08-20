package com.amaze.laputacloudslib.googledrive

import arrow.core.Either
import arrow.core.computations.either
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleDriveDriver(val googleDriveService: Drive): AbstractFileStructureDriver<GoogleDrivePath, GoogleDriveFile>() {
    override fun getRoot(): GoogleDrivePath {
        return GoogleDrivePath("", true)
    }

    override suspend fun getFiles(
        path: GoogleDrivePath,
        callback: suspend (Either<Exception, List<GoogleDriveFile>>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val result = either<Exception, List<GoogleDriveFile>> {
                val fileList: FileList =
                    googleDriveService.files().list().setSpaces("drive").execute()
                fileList.files.map {
                    GoogleDriveFile(it.name, GoogleDrivePath(it.id, false))
                }
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }

    override suspend fun getFile(path: GoogleDrivePath, callback: suspend (Either<Exception, GoogleDriveFile>) -> Unit) {
        TODO("Not yet implemented")
    }

}
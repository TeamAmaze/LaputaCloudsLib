package com.amaze.laputacloudslib.googledrive

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
        callback: suspend (List<GoogleDriveFile>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val fileList: FileList = googleDriveService.files().list().setSpaces("drive").execute()
            val processed = fileList.files.map {
                GoogleDriveFile(it.name, GoogleDrivePath(it.id, false))
            }

            withContext(Dispatchers.Main) {
                callback(processed)
            }
        }
    }

    override suspend fun getFile(path: GoogleDrivePath, callback: suspend (GoogleDriveFile) -> Unit) {
        TODO("Not yet implemented")
    }

}
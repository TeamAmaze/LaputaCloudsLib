package com.amaze.laputacloudslib.googledrive

import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.amaze.laputacloudslib.CloudPath
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleDriveDriver(val googleDriveService: Drive): AbstractFileStructureDriver() {
    override fun getRoot(): CloudPath {
        return GoogleDrivePath("", true)
    }

    override suspend fun getFiles(
        path: CloudPath,
        callback: suspend (List<AbstractCloudFile>) -> Unit
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

    override suspend fun getFile(path: CloudPath, callback: suspend (AbstractCloudFile) -> Unit) {
        TODO("Not yet implemented")
    }

}
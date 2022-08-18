package com.amaze.laputacloudslib.onedrive

import com.amaze.laputacloudslib.*
import com.amaze.laputacloudslib.AbstractCloudPath.Companion.SEPARATOR
import com.onedrive.sdk.extensions.IOneDriveClient

class OneDriveDriver(val oneDriveClient: IOneDriveClient) : AbstractFileStructureDriver<OneDrivePath, OneDriveCloudFile>() {
    companion object {
        const val SCHEME = "onedrive:"
    }

    override fun getRoot(): OneDrivePath {
        return OneDrivePath("/")
    }

    override suspend fun getFiles(path: OneDrivePath, callback: suspend (List<OneDriveCloudFile>) -> Unit) {
        oneDriveClient.drive.root.getItemWithPath(path.sanitizedPath).children.buildRequest().get(
            crashOnFailure { requestForFile ->
                callback(requestForFile.currentPage.map {
                    OneDriveCloudFile(
                        this@OneDriveDriver,
                        path.join(it.name),
                        it
                    )
                })
            })
    }

    override suspend fun getFile(path: OneDrivePath, callback: suspend (OneDriveCloudFile) -> Unit) {
        oneDriveClient.drive.root.getItemWithPath(path.sanitizedPath).buildRequest().get(crashOnFailure { requestForFile ->
            callback(
                OneDriveCloudFile(
                    this@OneDriveDriver,
                    path,
                    requestForFile,
                    isRootDirectory = path.sanitizedPath == SEPARATOR
                )
            )
        })
    }
}
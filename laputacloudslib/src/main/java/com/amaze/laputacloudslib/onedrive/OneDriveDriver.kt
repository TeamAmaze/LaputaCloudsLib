package com.amaze.laputacloudslib.onedrive

import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.amaze.laputacloudslib.CloudPath
import com.amaze.laputacloudslib.CloudPath.Companion.SEPARATOR
import com.amaze.laputacloudslib.CloudPath.Companion.crashyCheckAgainst
import com.amaze.laputacloudslib.OneDrivePath
import com.onedrive.sdk.extensions.IOneDriveClient

class OneDriveDriver(val oneDriveClient: IOneDriveClient) : AbstractFileStructureDriver() {
    companion object {
        const val SCHEME = "onedrive:"
    }

    override fun getRoot(): CloudPath {
        return OneDrivePath("/")
    }

    override suspend fun getFiles(path: CloudPath, callback: suspend (List<AbstractCloudFile>) -> Unit) {
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

    override suspend fun getFile(path: CloudPath, callback: suspend (AbstractCloudFile) -> Unit) {
        crashyCheckAgainst<OneDrivePath>(path)

        oneDriveClient.drive.root.getItemWithPath(path.sanitizedPath).buildRequest().get(crashOnFailure { requestForFile ->
            callback(
                OneDriveCloudFile(
                    this@OneDriveDriver,
                    path as OneDrivePath,
                    requestForFile,
                    isRootDirectory = path.sanitizedPath == SEPARATOR
                )
            )
        })
    }
}
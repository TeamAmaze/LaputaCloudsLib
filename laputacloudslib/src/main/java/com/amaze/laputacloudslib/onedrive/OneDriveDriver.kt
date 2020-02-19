package com.amaze.laputacloudslib.onedrive

import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.onedrive.sdk.extensions.IOneDriveClient

class OneDriveDriver(val oneDriveClient: IOneDriveClient) : AbstractFileStructureDriver() {
    companion object {
        const val SCHEME = "onedrive:"
    }

    override val SCHEME: String =
        Companion.SCHEME

    override suspend fun getFiles(path: String, callback: suspend (List<AbstractCloudFile>) -> Unit) {
        val rawPath =
            sanitizeRawPath(
                removeScheme(path)
            )

        oneDriveClient.drive.root.getItemWithPath(rawPath).children.buildRequest().get(
            crashOnFailure { requestForFile ->
                callback(requestForFile.currentPage.map {
                    OneDriveCloudFile(
                        this@OneDriveDriver,
                        rawPath + SEPARATOR + it.name,
                        it
                    )
                })
            })
    }

    override suspend fun getFile(path: String, callback: suspend (AbstractCloudFile) -> Unit) {
        val rawPath =
            sanitizeRawPath(
                removeScheme(path)
            )

        oneDriveClient.drive.root.getItemWithPath(rawPath).buildRequest().get(crashOnFailure { requestForFile ->
            callback(
                OneDriveCloudFile(
                    this@OneDriveDriver,
                    rawPath,
                    requestForFile,
                    isRootDirectory = rawPath == SEPARATOR
                )
            )
        })
    }
}
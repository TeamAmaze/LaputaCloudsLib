package com.amaze.laputacloudslib.onedrive

import arrow.core.Either
import arrow.core.computations.either
import com.amaze.laputacloudslib.*
import com.amaze.laputacloudslib.AbstractCloudPath.Companion.SEPARATOR
import com.onedrive.sdk.extensions.IOneDriveClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OneDriveDriver(val oneDriveClient: IOneDriveClient) : AbstractFileStructureDriver<OneDrivePath, OneDriveCloudFile>() {
    companion object {
        const val SCHEME = "onedrive:"
    }

    override fun getRoot(): OneDrivePath {
        return OneDrivePath("/")
    }

    override suspend fun getFiles(path: OneDrivePath, callback: suspend (Either<Exception, List<OneDriveCloudFile>>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                oneDriveClient.drive.root.getItemWithPath(path.sanitizedPath).children.buildRequest().get(
                    crashOnFailure { requestForFile ->
                        val result = either<Exception, List<OneDriveCloudFile>> {
                            requestForFile.bind().currentPage.map {
                                OneDriveCloudFile(
                                    this@OneDriveDriver,
                                    path.join(it.name),
                                    it
                                )
                            }
                        }

                        callback(result)
                    })
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    callback(Either.Left(e))
                }
            }
        }
    }

    override suspend fun getFile(path: OneDrivePath, callback: suspend (Either<Exception, OneDriveCloudFile>) -> Unit) {
        try {
            oneDriveClient.drive.root.getItemWithPath(path.sanitizedPath).buildRequest().get(
                crashOnFailure { requestForFile ->
                    val result = either<Exception, OneDriveCloudFile> {
                        OneDriveCloudFile(
                            this@OneDriveDriver,
                            path,
                            requestForFile.bind(),
                            isRootDirectory = path.sanitizedPath == SEPARATOR
                        )
                    }

                    callback(result)
                })
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                callback(Either.Left(e))
            }
        }
    }
}
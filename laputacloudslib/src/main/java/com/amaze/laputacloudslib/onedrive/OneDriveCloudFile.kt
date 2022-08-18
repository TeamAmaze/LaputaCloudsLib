package com.amaze.laputacloudslib.onedrive

import com.amaze.laputacloudslib.AbstractCloudFile
import com.onedrive.sdk.concurrency.IProgressCallback
import com.onedrive.sdk.core.ClientException
import com.onedrive.sdk.extensions.ChunkedUploadSessionDescriptor
import com.onedrive.sdk.extensions.Item
import com.onedrive.sdk.extensions.ItemReference
import com.onedrive.sdk.options.QueryOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class OneDriveCloudFile(
    val driver: OneDriveDriver,
    override val path: OneDrivePath,
    val item: Item,
    override val isRootDirectory: Boolean = false
): AbstractCloudFile<OneDrivePath, OneDriveCloudFile>() {
    override val name: String
        get() = item.name

    override val isDirectory: Boolean
        get() = item.folder != null

    override val byteSize = item.size

    override fun getParent(callback: suspend (OneDriveCloudFile?) -> Unit) {
        path

        if(isRootDirectory) {
            CoroutineScope(Dispatchers.Main).launch {
                callback(null)
            }
        }

        val guessedParentPath = path.getParentFromPath()

        driver.oneDriveClient.drive.root.getItemWithPath(guessedParentPath.sanitizedPath).buildRequest().get(
            crashOnFailure { result ->
                callback(
                    OneDriveCloudFile(
                        driver,
                        guessedParentPath,
                        result,
                        guessedParentPath.sanitizedPath == "/"
                    )
                )
            })
    }

    override fun delete(callback: () -> Unit) {
        driver.oneDriveClient.drive.root.getItemWithPath(path.sanitizedPath).buildRequest().delete(
            crashOnFailure {
                callback()
            })
    }

    override fun copyTo(newName: String, folder: OneDriveCloudFile, callback: (OneDriveCloudFile) -> Unit) {
        folder

        val parentReferenceForFolder = folder.item.toItemReference()

        driver.oneDriveClient.drive.getItems(item.id).getCopy(newName, parentReferenceForFolder).buildRequest().post(
            crashOnFailure { asyncMonitor ->
                asyncMonitor.pollForResult(750, object : IProgressCallback<Item> {
                    override fun success(result: Item) {
                        callback(OneDriveCloudFile(
                            driver,
                            folder.path.join(newName),
                            result,
                            false
                        ))
                    }

                    override fun failure(ex: ClientException) {
                        throw OneDriveIOException(ex)
                    }

                    override fun progress(current: Long, max: Long) = Unit

                })
            })
    }

    @Suppress("unused")
    fun copyToWithStatus(newName: String, folder: OneDriveCloudFile, callback: (OneDriveCopyStatus) -> Unit) {
        val parentReferenceForFolder = folder.item.toItemReference()

        driver.oneDriveClient.drive.getItems(item.id).getCopy(newName, parentReferenceForFolder).buildRequest().post(
            crashOnFailure { asyncMonitor ->
                callback(
                    OneDriveCopyStatus(
                        asyncMonitor
                    )
                )
            })
    }

    override fun moveTo(newName: String, folder: OneDriveCloudFile, callback: (OneDriveCloudFile) -> Unit) {
        val newItem = Item().also {
            it.name = newName
            it.parentReference = folder.item.toItemReference()
        }

        driver.oneDriveClient.drive.getItems(item.id).buildRequest().patch(newItem,
            crashOnFailure { item ->
                callback(
                    OneDriveCloudFile(
                        driver,
                        path,
                        item,
                        false
                    )
                )
            })
    }

    override fun download(callback: (InputStream) -> Unit) {
        driver.oneDriveClient.drive.getItems(item.id).content.buildRequest().get(crashOnFailure { inputStream ->
            callback(inputStream)
        })
    }

    override fun uploadHere(
        fileToUpload: OneDriveCloudFile,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: OneDriveCloudFile) -> Unit
    ) {
        fileToUpload.download { inputStream ->
            uploadHere(inputStream, fileToUpload.name, fileToUpload.byteSize, onProgress, callback)
        }
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: OneDriveCloudFile) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (size <= 3 * 1024 * 1024) {
                uploadAsSmallFile(inputStream, name, callback)
            } else if (size <= 150 * 1024 * 1024) {
                uploadAsBigFile(inputStream, name, size, onProgress, callback)
            } else {
                throw UnsupportedOperationException("Size too big to transfer!")
            }
        }
    }

    suspend fun uploadAsSmallFile(
        inputStream: InputStream,
        name: String,
        callback: (uploadedFile: OneDriveCloudFile) -> Unit
    )= withContext(Dispatchers.IO) {
        driver.oneDriveClient.drive
            .getItems(item.id)
            .children
            .byId(name)
            .content
            .buildRequest()
            .put(inputStream.readBytes(),
                crashOnFailure {
                    CoroutineScope(Dispatchers.Main).launch {
                        callback(
                            OneDriveCloudFile(
                                driver,
                                path.join(name),
                                it
                            )
                        )
                    }
                })
    }

    /**
     * File files in the range 3MB to 150MB
     * Uses a server side tracking system so that 4MB are transfered at a time
     */
    suspend fun uploadAsBigFile(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: OneDriveCloudFile) -> Unit
    ) = withContext(Dispatchers.IO) {
        val fileCompletePath = path.join(name)

        driver.oneDriveClient.drive.root.getItemWithPath(path.sanitizedPath)
            .getCreateSession(ChunkedUploadSessionDescriptor()).buildRequest().post()
            .createUploadProvider(
                driver.oneDriveClient,
                inputStream,
                size.toInt(),
                Item::class.java
            ).upload(
                listOf(
                    QueryOption(
                        "@name.conflictBehavior",
                        "fail"
                    )
                ),
                object :
                    IProgressCallback<Item> {
                    override fun progress(current: Long, max: Long) {
                        onProgress?.invoke(current)//TODO this should run in [Dispatchers::Main]
                    }

                    override fun success(result: Item) {
                        CoroutineScope(Dispatchers.Main).launch {
                            callback(
                                OneDriveCloudFile(
                                    driver,
                                    fileCompletePath,
                                    result
                                )
                            )
                        }
                    }

                    override fun failure(ex: ClientException) {
                        throw OneDriveIOException(
                            ex
                        )
                    }
                }
            )
    }

    companion object {
        private fun Item.toItemReference() = ItemReference().also {
            //Error message: id and path in parentReference should not both be specified
            it.id = this.id
            it.driveId = this.parentReference.driveId
        }
    }
}
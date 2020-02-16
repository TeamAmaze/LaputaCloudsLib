package com.amaze.laputacloudslib.onedrive

import androidx.annotation.WorkerThread
import com.amaze.laputacloudslib.AbstractCloudCopyStatus
import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.onedrive.sdk.concurrency.IProgressCallback
import com.onedrive.sdk.core.ClientException
import com.onedrive.sdk.extensions.ChunkedUploadSessionDescriptor
import com.onedrive.sdk.extensions.Item
import com.onedrive.sdk.extensions.ItemReference
import com.onedrive.sdk.options.QueryOption
import java.io.File
import java.io.InputStream

class OneDriveCloudFile(
    val driver: OneDriveDriver,
    override val path: String,
    val item: Item,
    override val isRootDirectory: Boolean = false
): AbstractCloudFile() {
    override val name: String
        get() = item.name

    override val isDirectory: Boolean
        get() = item.folder != null

    override val byteSize = item.size

    override fun getParent(callback: (AbstractCloudFile?) -> Unit) {
        if(isRootDirectory) {
            callback(null)
        }

        val guessedParentPath = getParentFromPath(
            path
        )!!

        driver.oneDriveClient.drive.root.getItemWithPath(guessedParentPath).buildRequest().get(
            crashOnFailure { result ->
                callback(
                    OneDriveCloudFile(
                        driver,
                        guessedParentPath,
                        result,
                        guessedParentPath == "/"
                    )
                )
            })
    }

    override fun delete(callback: () -> Unit) {
        driver.oneDriveClient.drive.root.getItemWithPath(path).buildRequest().delete(
            crashOnFailure {
                callback()
            })
    }

    override fun copyTo(newName: String, folder: AbstractCloudFile, callback: (AbstractCloudCopyStatus) -> Unit) {
        folder as OneDriveCloudFile

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

    override fun moveTo(newName: String, folder: AbstractCloudFile, callback: (AbstractCloudFile) -> Unit) {
        folder as OneDriveCloudFile

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

    @WorkerThread
    override fun uploadHere(
        fileToUpload: AbstractCloudFile,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        fileToUpload.download { inputStream ->
            uploadHere(inputStream, fileToUpload.name, fileToUpload.byteSize, callback)
        }
    }

    @WorkerThread
    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        if(size <= 3*1024*1024) {
            uploadAsSmallFile(inputStream, name, callback)
        } else if(size <= 150*1024*1024){
            uploadAsBigFile(inputStream, name, size, callback)
        } else {
            throw UnsupportedOperationException("Size too big to transfer!")
        }
    }

    fun uploadAsSmallFile(
        inputStream: InputStream,
        name: String,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        driver.oneDriveClient.drive
            .getItems(item.id)
            .children
            .byId(name)
            .content
            .buildRequest()
            .put(inputStream.readBytes(),
                crashOnFailure {
                    callback(
                        OneDriveCloudFile(
                            driver,
                            path + AbstractFileStructureDriver.SEPARATOR + name,
                            it
                        )
                    )
                })
    }

    /**
     * File files in the range 3MB to 150MB
     * Uses a server side tracking system so that 4MB are transfered at a time
     */
    @WorkerThread
    fun uploadAsBigFile(
        inputStream: InputStream,
        name: String,
        size: Long,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        val fileCompletePath = path + AbstractFileStructureDriver.SEPARATOR + name

        driver.oneDriveClient.drive.root.getItemWithPath(path)
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
                    override fun progress(current: Long, max: Long) = Unit

                    override fun success(result: Item) {
                        callback(
                            OneDriveCloudFile(
                                driver,
                                fileCompletePath,
                                result
                            )
                        )
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
        @JvmStatic
        private fun getParentFromPath(path: String): String? = File(path).parent

        private fun Item.toItemReference() = ItemReference().also {
            //Error message: id and path in parentReference should not both be specified
            it.id = this.id
            it.driveId = this.parentReference.driveId
        }
    }
}
package com.amaze.laputacloudslib.onedrive

import arrow.core.Either
import arrow.core.computations.either
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

//TODO check for crash everywhere
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

    override fun getParent(callback: suspend (Either<Exception, OneDriveCloudFile>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val guessedParentPath = either<Exception, OneDrivePath> {
                if (isRootDirectory) {
                    throw Exception("Is root")
                }

                path.getParentFromPath()
            }

            when (guessedParentPath) {
                is Either.Left -> {
                    withContext(Dispatchers.Main) {
                        callback(guessedParentPath)
                    }
                }
                is Either.Right -> {
                    driver.oneDriveClient.drive.root.getItemWithPath(guessedParentPath.value.sanitizedPath)
                        .buildRequest()
                        .get(
                            crashOnFailure { parent: Either<Exception, Item> ->
                                val result = either<Exception, OneDriveCloudFile> {
                                    OneDriveCloudFile(
                                        driver,
                                        guessedParentPath.value,
                                        parent.bind(),
                                        guessedParentPath.value.sanitizedPath == "/"
                                    )
                                }

                                callback(result)
                            })
                }
            }
        }
    }

    override fun delete(callback: (Either<Exception, Unit>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            driver.oneDriveClient.drive
                .root
                .getItemWithPath(path.sanitizedPath)
                .buildRequest()
                .delete(crashOnFailure {
                    callback(it.map { Unit })
                })
        }
    }

    override fun copyTo(newName: String, folder: OneDriveCloudFile, callback: (Either<Exception, OneDriveCloudFile>) -> Unit) {
        val parentReferenceForFolder = folder.item.toItemReference()

        driver.oneDriveClient.drive.getItems(item.id).getCopy(newName, parentReferenceForFolder).buildRequest().post(
            crashOnFailure { asyncMonitor ->
                when(asyncMonitor) {
                    is Either.Left -> {
                        callback(asyncMonitor)
                    }
                    is Either.Right -> {
                        asyncMonitor.value.pollForResult(750, object : IProgressCallback<Item> {
                            override fun success(item: Item) {
                                val result = OneDriveCloudFile(
                                    driver,
                                    folder.path.join(newName),
                                    item,
                                    false
                                )

                                callback(Either.Right(result))
                            }

                            override fun failure(ex: ClientException) {
                                callback(Either.Left(OneDriveIOException(ex)))
                            }

                            override fun progress(current: Long, max: Long) = Unit
                        })
                    }
                }


            })
    }

    @Suppress("unused")
    fun copyToWithStatus(newName: String, folder: OneDriveCloudFile, callback: (Either<Exception, OneDriveCopyStatus>) -> Unit) {
        val parentReferenceForFolder = folder.item.toItemReference()

        driver.oneDriveClient.drive
            .getItems(item.id)
            .getCopy(newName, parentReferenceForFolder)
            .buildRequest()
            .post(crashOnFailure { asyncMonitor ->
                val result = when (asyncMonitor) {
                    is Either.Left -> asyncMonitor
                    is Either.Right -> Either.Right(OneDriveCopyStatus(asyncMonitor.value))
                }

                callback(result)
            })
    }

    override fun moveTo(newName: String, folder: OneDriveCloudFile, callback: (Either<Exception, OneDriveCloudFile>) -> Unit) {
        val newItem = Item().also {
            it.name = newName
            it.parentReference = folder.item.toItemReference()
        }

        driver.oneDriveClient.drive
            .getItems(item.id)
            .buildRequest()
            .patch(newItem, crashOnFailure { item ->
                val result = either<Exception, OneDriveCloudFile> {
                    OneDriveCloudFile(
                        driver,
                        path,
                        item.bind(),
                        false
                    )
                }

                callback(result)
            })
    }

    override fun download(callback: (Either<Exception, InputStream>) -> Unit) {
        driver.oneDriveClient.drive
            .getItems(item.id)
            .content
            .buildRequest()
            .get(crashOnFailure(callback))
    }

    override fun uploadHere(
        fileToUpload: OneDriveCloudFile,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: Either<Exception, OneDriveCloudFile>) -> Unit
    ) {
        fileToUpload.download { inputStream ->
            when (inputStream) {
                is Either.Left -> {
                    callback(inputStream)
                }
                is Either.Right -> {
                    uploadHere(
                        inputStream.value,
                        fileToUpload.name,
                        fileToUpload.byteSize,
                        onProgress,
                        callback
                    )
                }
            }

        }
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: Either<Exception, OneDriveCloudFile>) -> Unit
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
        callback: (uploadedFile: Either<Exception, OneDriveCloudFile>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            driver.oneDriveClient.drive
                .getItems(item.id)
                .children
                .byId(name)
                .content
                .buildRequest()
                .put(inputStream.readBytes(), crashOnFailure {
                    val result = either<Exception, OneDriveCloudFile> {
                        OneDriveCloudFile(
                            driver,
                            path.join(name),
                            it.bind()
                        )
                    }

                    callback(result)
                })
        }
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
        callback: (uploadedFile: Either<Exception, OneDriveCloudFile>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
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
                            CoroutineScope(Dispatchers.Main).launch {
                                onProgress?.invoke(current)
                            }
                        }

                        override fun success(item: Item) {
                            CoroutineScope(Dispatchers.Main).launch {
                                val result = OneDriveCloudFile(
                                    driver,
                                    fileCompletePath,
                                    item
                                )

                                callback(Either.Right(result))
                            }
                        }

                        override fun failure(ex: ClientException) {
                            CoroutineScope(Dispatchers.Main).launch {
                                callback(Either.Left(OneDriveIOException(ex)))
                            }
                        }
                    }
                )
        }
    }

    companion object {
        private fun Item.toItemReference() = ItemReference().also {
            //Error message: id and path in parentReference should not both be specified
            it.id = this.id
            it.driveId = this.parentReference.driveId
        }
    }
}
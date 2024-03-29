package com.amaze.laputacloudslib.dropbox

import arrow.core.Either
import arrow.core.computations.either
import com.amaze.laputacloudslib.AbstractCloudFile
import com.dropbox.core.DbxException
import com.dropbox.core.NetworkIOException
import com.dropbox.core.RetryException
import com.dropbox.core.util.IOUtil
import com.dropbox.core.v2.files.*
import kotlinx.coroutines.*
import java.io.InputStream

class DropBoxFile(
    val driver: DropBoxDriver,
    override val path: DropBoxPath,
    override val isRootDirectory: Boolean,
    override val name: String,
    override val isDirectory: Boolean
) : AbstractCloudFile<DropBoxPath, DropBoxFile>() {
    override val byteSize: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun getParent(callback: suspend (Either<Exception, DropBoxFile>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            driver.getFile(path.getParentFromPath(), callback)
        }
    }

    override fun delete(callback: (Either<Exception, Unit>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = either<Exception, Unit> {
                driver.client.files().deleteV2(path.sanitizedPathOrRoot)
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
    }

    override fun copyTo(
        newName: String,
        folder: DropBoxFile,
        callback: (Either<Exception, DropBoxFile>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result =  either<Exception, DropBoxFile> {
                driver.client
                    .files()
                    .copyV2(path.sanitizedPathOrRoot, folder.path.join(newName).sanitizedPath)
                    .metadata
                    .toDropBoxFile(driver)
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
    }

    override fun moveTo(
        newName: String,
        folder: DropBoxFile,
        callback: (Either<Exception, DropBoxFile>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = either<Exception, DropBoxFile> {
                driver.client
                    .files()
                    .moveV2(path.sanitizedPathOrRoot, folder.path.join(newName).sanitizedPath)
                    .metadata
                    .toDropBoxFile(driver)
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
    }

    override fun download(callback: (Either<Exception, InputStream>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = either<Exception, InputStream> {
                driver.client.files().download(path.sanitizedPath).inputStream
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
    }

    override fun uploadHere(
        fileToUpload: DropBoxFile,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: Either<Exception, DropBoxFile>) -> Unit
    ) {
        fileToUpload.download { inputStream ->
            when (inputStream) {
                is Either.Right -> {
                    uploadHere(
                        inputStream.value,
                        fileToUpload.name,
                        fileToUpload.byteSize,
                        onProgress,
                        callback
                    )
                }
                is Either.Left -> {
                    callback(inputStream)
                }
            }
        }
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: Either<Exception, DropBoxFile>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (size < 8 * 1024 * 1024) {
                uploadAsSmallFile(inputStream, name, callback)
            } else {
                uploadAsBigFile(inputStream, name, size, onProgress, callback)
            }
        }
    }

    suspend fun uploadAsSmallFile(
        inputStream: InputStream,
        name: String,
        callback: (uploadedFile: Either<Exception, DropBoxFile>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val result = either<Exception, DropBoxFile> {
                inputStream.use {
                    driver.client.files().uploadBuilder(path.join(name).sanitizedPath)
                        .withMode(WriteMode.ADD)
                        .uploadAndFinish(it)
                }.toDropBoxFile(driver)
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }
    /**
     * Adjust the chunk size based on your network speed and reliability. Larger chunk sizes will
     * result in fewer network requests, which will be faster. But if an error occurs, the entire
     * chunk will be lost and have to be re-uploaded. Use a multiple of 4MiB for your chunk size.
     */
    private val CHUNKED_UPLOAD_CHUNK_SIZE = 8L shl 20 // 8MiB

    private val CHUNKED_UPLOAD_MAX_ATTEMPTS = 5

    /**
     * File files in the range 3MB to 150MB
     * Uses a server side tracking system so that 4MB are transfered at a time
     */
    suspend fun uploadAsBigFile(
        inputStream: InputStream,
        name: String,
        size: Long,
        progressCallback: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: Either<Exception, DropBoxFile>) -> Unit
    ) =
        withContext(Dispatchers.IO) {
            val fileCompletePath = path.join(name)
            val dbxClient = driver.client

            var uploaded = 0L
            var thrown: DbxException? = null
            var success: DropBoxFile? = null

            val progressListener: IOUtil.ProgressListener =
                object : IOUtil.ProgressListener {
                    var uploadedBytes: Long = 0
                    override fun onProgress(l: Long) {
                        CoroutineScope(Dispatchers.Main)
                            .launch {
                                progressCallback?.invoke(l + uploadedBytes)
                            }
                        if (l == CHUNKED_UPLOAD_CHUNK_SIZE) uploadedBytes += CHUNKED_UPLOAD_CHUNK_SIZE
                    }
                }

            // Chunked uploads have 3 phases, each of which can accept uploaded bytes:
            //
            //    (1)  Start: initiate the upload and get an upload session ID
            //    (2) Append: upload chunks of the file to append to our session
            //    (3) Finish: commit the upload and close the session
            //
            // We track how many bytes we uploaded to determine which phase we should be in.
            var sessionId: String? = null
            for (i in 0 until CHUNKED_UPLOAD_MAX_ATTEMPTS) {
                try {
                    inputStream.use { fileStream ->
                        // if this is a retry, make sure seek to the correct offset
                        fileStream.skip(uploaded)
                        // (1) Start
                        if (sessionId == null) {
                            sessionId = dbxClient.files().uploadSessionStart()
                                .uploadAndFinish(
                                    fileStream,
                                    CHUNKED_UPLOAD_CHUNK_SIZE,
                                    progressListener
                                )
                                .sessionId
                            uploaded += CHUNKED_UPLOAD_CHUNK_SIZE
                            withContext(Dispatchers.Main) {
                                progressCallback?.invoke(uploaded)
                            }
                        }
                        var cursor =
                            UploadSessionCursor(
                                sessionId,
                                uploaded
                            )
                        // (2) Append
                        while (size - uploaded > CHUNKED_UPLOAD_CHUNK_SIZE) {
                            dbxClient.files().uploadSessionAppendV2(cursor)
                                .uploadAndFinish(
                                    fileStream,
                                    CHUNKED_UPLOAD_CHUNK_SIZE,
                                    progressListener
                                )
                            uploaded += CHUNKED_UPLOAD_CHUNK_SIZE
                            withContext(Dispatchers.Main) {
                                progressCallback?.invoke(uploaded)
                            }
                            cursor =
                                UploadSessionCursor(
                                    sessionId,
                                    uploaded
                                )
                        }
                        // (3) Finish
                        val remaining = size - uploaded
                        val commitInfo =
                            CommitInfo.newBuilder(
                                fileCompletePath.sanitizedPath
                            )
                                .withMode(WriteMode.ADD)
                                .build()

                        val result = dbxClient.files()
                                .uploadSessionFinish(cursor, commitInfo)
                                .uploadAndFinish(fileStream, remaining, progressListener)
                                .toDropBoxFile(driver)

                        success = result
                    }
                } catch (ex: RetryException) {
                    thrown = ex
                    // RetryExceptions are never automatically retried by the client for uploads. Must
                    // catch this exception even if DbxRequestConfig.getMaxRetries() > 0.
                    delay(ex.backoffMillis)
                    continue
                } catch (ex: NetworkIOException) {
                    thrown = ex
                    // network issue with Dropbox (maybe a timeout?) try again
                    continue
                } catch (ex: UploadSessionLookupErrorException) {
                    if (ex.errorValue.isIncorrectOffset) {
                        thrown = ex
                        // server offset into the stream doesn't match our offset (uploaded). Seek to
                        // the expected offset according to the server and try again.
                        uploaded = ex.errorValue
                            .incorrectOffsetValue
                            .correctOffset
                        continue
                    } else { // Some other error occurred, give up.
                        throw DropBoxIOException(
                            ex
                        )
                    }
                } catch (ex: UploadSessionFinishErrorException) {
                    if (ex.errorValue.isLookupFailed && ex.errorValue.lookupFailedValue.isIncorrectOffset) {
                        thrown = ex
                        // server offset into the stream doesn't match our offset (uploaded). Seek to
                        // the expected offset according to the server and try again.
                        uploaded = ex.errorValue
                            .lookupFailedValue
                            .incorrectOffsetValue
                            .correctOffset
                        continue
                    } else { // some other error occurred, give up.
                        throw DropBoxIOException(
                            ex
                        )
                    }
                } catch (ex: DbxException) {
                    throw DropBoxIOException(ex)
                }
            }

            val result = either<Exception, DropBoxFile> {
                val success = success

                if(success == null) {
                    if (thrown != null) {
                        throw TooManyFailsException(
                            "Maxed out upload attempts to Dropbox. Most recent error stack trace",
                            thrown
                        )
                    } else {
                        throw TooManyFailsException(
                            "Maxed out upload attempts to Dropbox"
                        )
                    }
                }

                success
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }

}
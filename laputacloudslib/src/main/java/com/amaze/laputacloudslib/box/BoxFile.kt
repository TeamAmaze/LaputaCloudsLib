package com.amaze.laputacloudslib.box

import arrow.core.Either
import com.amaze.laputacloudslib.AbstractCloudFile
import com.box.androidsdk.content.BoxApiFile
import com.box.androidsdk.content.listeners.ProgressListener
import com.box.androidsdk.content.models.BoxItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class BoxFile(
    private val fileApi: BoxApiFile,
    private val info: BoxItem,
    override val path: BoxPath
): AbstractCloudFile<BoxPath, BoxFile>() {
    override val name: String = info.name ?: "root"
    override val isDirectory: Boolean = path.isDirectory
    override val isRootDirectory: Boolean = path.isRoot
    override val byteSize: Long
        get() = info.size

    override fun getParent(callback: suspend (Either<Exception, BoxFile>) -> Unit) {
        if(path.isRoot) {
            CoroutineScope(Dispatchers.Main).launch {
                callback(Either.Left(Exception("Is root!")))
            }
        }

        val file: BoxFile

        try {
            val parent = info.parent
            val guessedParentPath = path.getParentPathFromPath()
            file = BoxFile(fileApi, parent, guessedParentPath)
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                callback(Either.Left(e))
            }
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            callback(Either.Right(file))
        }
    }

    override fun delete(callback: (Either<Exception, Unit>) -> Unit) {
        CoroutineScope(Dispatchers.IO)
            .launch {
                try {
                    fileApi.getDeleteRequest(info.id).send()
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        callback(Either.Left(e))
                    }
                    return@launch
                }

                CoroutineScope(Dispatchers.Main)
                    .launch {
                        callback(Either.Right(Unit))
                    }
            }
    }

    override fun copyTo(
        newName: String,
        folder: BoxFile,
        callback: (Either<Exception, BoxFile>) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun moveTo(
        newName: String,
        folder: BoxFile,
        callback: (Either<Exception, BoxFile>) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun download(callback: (Either<Exception, InputStream>) -> Unit) {
        CoroutineScope(Dispatchers.IO)
            .launch {
                val input = PipedInputStream()
                val out = PipedOutputStream(input)

                try {
                    fileApi.getDownloadRequest(out, info.id).send()
                } catch (e: Exception) {
                    callback(Either.Left(e))
                    return@launch
                }

                CoroutineScope(Dispatchers.Main)
                    .launch {
                        callback(Either.Right(input))
                    }
            }
    }

    override fun uploadHere(
        fileToUpload: BoxFile,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: Either<Exception, BoxFile>) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: Either<Exception, BoxFile>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO)
            .launch {
                val uploadedFile: com.box.androidsdk.content.models.BoxFile? = try {
                    fileApi.getUploadRequest(
                        inputStream,
                        name,
                        info.id
                    )
                        .setProgressListener { numBytes, _ ->
                            CoroutineScope(Dispatchers.Main)
                                .launch {
                                    onProgress?.invoke(numBytes)
                                }
                        }
                        .send()
                } catch (e: Exception) {
                    callback(Either.Left(e))
                    return@launch
                }

                if(uploadedFile == null) {
                    callback(Either.Left(BoxAccountException("Error uploading")))
                    return@launch
                }

                CoroutineScope(Dispatchers.Main)
                    .launch {
                        callback(Either.Right(BoxFile(fileApi, uploadedFile, path.join(name))))
                    }
            }
    }

}
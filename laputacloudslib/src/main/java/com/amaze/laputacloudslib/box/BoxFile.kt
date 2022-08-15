package com.amaze.laputacloudslib.box

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
): AbstractCloudFile() {
    override val name: String = info.name ?: "root"
    override val isDirectory: Boolean = path.isDirectory
    override val isRootDirectory: Boolean = path.isRoot
    override val byteSize: Long
        get() = info.size

    override fun getParent(callback: suspend (AbstractCloudFile?) -> Unit) {
        val parent = info.parent
        val guessedParentPath = path.getParentPathFromPath()

        CoroutineScope(Dispatchers.Main).launch {
            callback(BoxFile(fileApi, parent, guessedParentPath))
        }
    }

    override fun delete(callback: () -> Unit) {
        CoroutineScope(Dispatchers.IO)
            .launch {
                fileApi.getDeleteRequest(info.id).send()

                CoroutineScope(Dispatchers.Main)
                    .launch {
                        callback()
                    }
            }
    }

    override fun copyTo(
        newName: String,
        folder: AbstractCloudFile,
        callback: (AbstractCloudFile) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun moveTo(
        newName: String,
        folder: AbstractCloudFile,
        callback: (AbstractCloudFile) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun download(callback: (InputStream) -> Unit) {
        CoroutineScope(Dispatchers.IO)
            .launch {
                val input = PipedInputStream()
                val out = PipedOutputStream(input)
                fileApi.getDownloadRequest(out, info.id).send()

                CoroutineScope(Dispatchers.Main)
                    .launch {
                        callback(input)
                    }
            }
    }

    override fun uploadHere(
        fileToUpload: AbstractCloudFile,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO)
            .launch {
                val uploadedFile: com.box.androidsdk.content.models.BoxFile? = fileApi.getUploadRequest(
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

                uploadedFile ?: throw BoxAccountException("Error uploading")

                CoroutineScope(Dispatchers.Main)
                    .launch {
                        callback(BoxFile(fileApi, uploadedFile, path.join(name)))
                    }
            }
    }

}
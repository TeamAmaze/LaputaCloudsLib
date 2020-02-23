package com.amaze.laputacloudslib

import com.amaze.laputacloudslib.dropbox.toDropBoxFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream

abstract class AbstractCloudFile {
    abstract val name: String
    abstract val path: CloudPath
    abstract val isDirectory: Boolean
    abstract val isRootDirectory: Boolean
    abstract val byteSize: Long

    abstract fun getParent(callback: suspend (AbstractCloudFile?) -> Unit)
    abstract fun delete(callback: () -> Unit)
    abstract fun copyTo(newName: String, folder: AbstractCloudFile, callback: (AbstractCloudFile) -> Unit)
    abstract fun moveTo(newName: String, folder: AbstractCloudFile, callback: (AbstractCloudFile) -> Unit)
    /**
     * You need to close the [InputStream] when not using it anymore
     */
    abstract fun download(callback: (InputStream) -> Unit)
    abstract fun uploadHere(fileToUpload: AbstractCloudFile, callback: (uploadedFile: AbstractCloudFile) -> Unit)
    abstract fun uploadHere(inputStream: InputStream, name: String, size: Long, callback: (uploadedFile: AbstractCloudFile) -> Unit)
}

class DropBoxFile(
    val driver: DropBoxDriver,
    override val path: DropBoxPath,
    override val isRootDirectory: Boolean,
    override val name: String,
    override val isDirectory: Boolean
) : AbstractCloudFile() {
    override val byteSize: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun getParent(callback: suspend (AbstractCloudFile?) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            //see https://www.dropboxforum.com/t5/API-Support-Feedback/Get-parent-folder/td-p/247874
            driver.getFile(path.getParentFromPath(), callback)
        }
    }

    override fun delete(callback: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            driver.client.files().deleteV2(path.sanitizedPathOrRoot)

            CoroutineScope(Dispatchers.Main).launch {
                callback()
            }
        }
    }

    override fun copyTo(
        newName: String,
        folder: AbstractCloudFile,
        callback: (AbstractCloudFile) -> Unit
    ) {
        folder as DropBoxFile

        CoroutineScope(Dispatchers.IO).launch {
            val result = driver.client.files().copyV2(path.sanitizedPathOrRoot, folder.path.join(newName).sanitizedPath)

            CoroutineScope(Dispatchers.Main).launch {
                callback(result.metadata.toDropBoxFile(driver))
            }
        }
    }

    override fun moveTo(
        newName: String,
        folder: AbstractCloudFile,
        callback: (AbstractCloudFile) -> Unit
    ) {
        folder as DropBoxFile

        CoroutineScope(Dispatchers.IO).launch {
            val result = driver.client.files().moveV2(path.sanitizedPathOrRoot, folder.path.join(newName).sanitizedPath)

            CoroutineScope(Dispatchers.Main).launch {
                callback(result.metadata.toDropBoxFile(driver))
            }
        }
    }

    override fun download(callback: (InputStream) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = driver.client.files().download(path.sanitizedPath)

            CoroutineScope(Dispatchers.Main).launch {
                callback(result.inputStream)
            }
        }
    }

    override fun uploadHere(
        fileToUpload: AbstractCloudFile,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
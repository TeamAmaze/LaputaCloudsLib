package com.amaze.laputacloudsapp.appfolder

import android.os.Build
import android.os.Environment
import androidx.annotation.FloatRange
import com.amaze.laputacloudslib.*
import com.amaze.laputacloudslib.AbstractCloudCopyStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream

class PhoneFile(
    override val path: CloudPath
) : AbstractCloudFile() {
    val file = File(path.sanitizedPath)

    override val name = file.name
    override val isDirectory = file.isDirectory
    override val isRootDirectory = path.sanitizedPath.isEmpty()
    override val byteSize = file.length()

    override fun getParent(callback: suspend (AbstractCloudFile?) -> Unit) {
        val parentPath = file.parent
        CoroutineScope(Dispatchers.Main).launch {
            callback(if (parentPath == null) null else PhoneFile(PhonePath(parentPath)))
        }
    }

    override fun delete(callback: () -> Unit) {
        if(!file.delete()) {
            throw IOException("Delete failed")
        }

        callback()
    }

    override fun copyTo(
        newName: String,
        folder: AbstractCloudFile,
        callback: (AbstractCloudCopyStatus) -> Unit
    ) {
        folder as PhoneFile

        val targetFile = File(folder.file, newName)

        file.copyTo(targetFile)

        callback(object : AbstractCloudCopyStatus() {
            override fun getStatus() = DONE

            @FloatRange(from = 0.0, to = 100.0)
            override fun getPercentage(): Float = 100.0f
        })
    }

    override fun moveTo(
        newName: String,
        folder: AbstractCloudFile,
        callback: (AbstractCloudFile) -> Unit
    ) {
        copyTo(newName, folder, {})
        file.delete()

        folder as PhoneFile

        val targetFile = File(folder.file, newName)
        callback(PhoneFile(PhonePath(targetFile.canonicalPath)))
    }

    override fun download(callback: (InputStream) -> Unit) {
        callback(file.inputStream())
    }

    override fun uploadHere(
        fileToUpload: AbstractCloudFile,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        throw NotImplementedError("Use copyTo or moveTo")
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        callback: (uploadedFile: AbstractCloudFile) -> Unit
    ) {
        throw NotImplementedError("Use copyTo or moveTo")
    }

}

class PhoneDriver : AbstractFileStructureDriver() {
    override fun getRoot(): CloudPath {
        return PhonePath("/")
    }

    override suspend fun getFiles(path: CloudPath, callback: suspend (List<AbstractCloudFile>) -> Unit) {
        callback(File(path.sanitizedPath).listFiles()!!.map {  it: File ->
            PhoneFile(PhonePath(it.canonicalPath))
        })
    }

    override suspend fun getFile(path: CloudPath, callback: suspend (AbstractCloudFile) -> Unit) {
        callback(PhoneFile(PhonePath(File(path.sanitizedPath).canonicalPath)))
    }
}

class PhonePath(path: String) : CloudPath(path) {
    private val falseRoot = getStartingFile().canonicalPath
    override val scheme: String = falseRoot

    override fun createInstanceOfSubclass(path: String) = PhonePath(path)

    private fun getStartingFile(): File {
        lateinit var externalDir: File

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (isEmulator()) {
                externalDir = File("/sdcard/")
            } else {
                val externalFile = Environment.getExternalStorageDirectory()
                    ?: throw IOException("Failed to read files")
                externalDir = File(externalFile.path)
            }
        } else {
            throw IllegalStateException("No support for Android Q")
        }

        externalDir.setReadable(true)
        return externalDir
    }

    private fun isEmulator() = (Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
            || "google_sdk" == Build.PRODUCT)
}

class PhoneUser : AbstractUser<PhoneDriver>() {
    override suspend fun getFileStructureDriverAsync(callback: suspend (PhoneDriver) -> Unit) {
        callback(PhoneDriver())
    }
}

class PhoneAccount : AbstractAccount() {
    override suspend fun tryLogInAsync(callback: suspend (AbstractUser<out AbstractFileStructureDriver>) -> Unit) {
        callback(PhoneUser())
    }
}
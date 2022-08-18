package com.amaze.laputacloudsapp.appfolder

import android.os.Build
import android.os.Environment
import com.amaze.laputacloudsapp.appfolder.PhoneDriver.Companion.FALSE_ROOT
import com.amaze.laputacloudslib.*
import com.amaze.laputacloudslib.AbstractCloudPath.Companion.SEPARATOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream

class PhoneFile(
    override val path: PhonePath
) : AbstractCloudFile<PhonePath, PhoneFile>() {
    val file = path.toFile()

    override val name = file.name
    override val isDirectory = file.isDirectory
    override val isRootDirectory = path.sanitizedPath == SEPARATOR
    override val byteSize = file.length()

    override fun getParent(callback: suspend (PhoneFile?) -> Unit) {
        val parentPath = file.parentFile?.toPhonePath()
        CoroutineScope(Dispatchers.Main).launch {
            callback(if (parentPath == null) null else PhoneFile(parentPath))
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
        folder: PhoneFile,
        callback: (PhoneFile) -> Unit
    ) {
        folder

        val targetFile = File(folder.file, newName)

        file.copyTo(targetFile)

        callback(PhoneFile(targetFile.toPhonePath()))
    }

    override fun moveTo(
        newName: String,
        folder: PhoneFile,
        callback: (PhoneFile) -> Unit
    ) {
        copyTo(newName, folder) {
            file.delete()

            folder

            val targetFile = File(folder.file, newName)
            callback(PhoneFile(targetFile.toPhonePath()))
        }
    }

    override fun download(callback: (InputStream) -> Unit) {
        callback(file.inputStream())
    }

    override fun uploadHere(
        fileToUpload: PhoneFile,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: PhoneFile) -> Unit
    ) {
        throw NotImplementedError("Use copyTo or moveTo")
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: PhoneFile) -> Unit
    ) {
        throw NotImplementedError("Use copyTo or moveTo")
    }

}

class PhoneDriver : AbstractFileStructureDriver<PhonePath, PhoneFile>() {
    companion object {
        val FALSE_ROOT = getStartingFile().canonicalPath

        @JvmStatic
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

        @JvmStatic
        private fun isEmulator() = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    override fun getRoot(): PhonePath {
        return PhonePath("/")
    }

    override suspend fun getFiles(path: PhonePath, callback: suspend (List<PhoneFile>) -> Unit) {
        path

        callback(path.toFile().listFiles()!!.map {  it: File ->
            PhoneFile(it.toPhonePath())
        })
    }

    override suspend fun getFile(path: PhonePath, callback: suspend (PhoneFile) -> Unit) {
        callback(PhoneFile(File(path.fullPath).toPhonePath()))
    }
}

fun File.toPhonePath(): PhonePath {
    return PhonePath(this.canonicalPath.substringAfter(FALSE_ROOT))
}

class PhonePath(path: String) : AbstractCloudPath<PhonePath>(path) {
    override val scheme: String = FALSE_ROOT

    fun toFile() = File(fullPath)
}

class PhoneAccount : AbstractAccount<PhonePath, PhoneFile, PhoneDriver>() {
    override suspend fun tryLogInAsync(callback: suspend (PhoneDriver) -> Unit) {
        callback(PhoneDriver())
    }
}
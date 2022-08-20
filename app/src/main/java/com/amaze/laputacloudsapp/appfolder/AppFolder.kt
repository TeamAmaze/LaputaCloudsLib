package com.amaze.laputacloudsapp.appfolder

import android.os.Build
import android.os.Environment
import arrow.core.Either
import arrow.core.computations.ResultEffect.bind
import arrow.core.computations.either
import arrow.core.computations.result
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

    override fun getParent(callback: suspend (Either<Exception, PhoneFile>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = either<Exception, PhoneFile> {
                val parentPath = file.parentFile?.toPhonePath() ?: throw Exception("No parent")
                PhoneFile(parentPath)
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
    }

    override fun delete(callback: (Either<Exception, Unit>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = either<Exception, Unit> {
                if (!file.delete()) {
                    throw IOException("Delete failed")
                }

                Unit
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
    }

    override fun copyTo(
        newName: String,
        folder: PhoneFile,
        callback: (Either<Exception, PhoneFile>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = either<Exception, PhoneFile> {
                val targetFile = File(folder.file, newName)
                file.copyTo(targetFile)
                PhoneFile(targetFile.toPhonePath())
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
    }

    override fun moveTo(
        newName: String,
        folder: PhoneFile,
        callback: (Either<Exception, PhoneFile>) -> Unit
    ) {
        copyTo(newName, folder) { it ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = either<Exception, PhoneFile> {
                    it.bind()

                    val deleted = file.delete()

                    if (!deleted) {
                        throw Exception("Error deleting")
                    }

                    val targetFile = File(folder.file, newName)
                    PhoneFile(targetFile.toPhonePath())
                }

                CoroutineScope(Dispatchers.Main).launch {
                    callback(result)
                }
            }
        }
    }

    override fun download(callback: (Either<Exception, InputStream>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = either<Exception, InputStream> {
                file.inputStream()
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
    }

    override fun uploadHere(
        fileToUpload: PhoneFile,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: Either<Exception, PhoneFile>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = either<Exception, PhoneFile> {
                throw NotImplementedError("Use copyTo or moveTo")
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
    }

    override fun uploadHere(
        inputStream: InputStream,
        name: String,
        size: Long,
        onProgress: ((bytes: Long) -> Unit)?,
        callback: (uploadedFile: Either<Exception, PhoneFile>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = either<Exception, PhoneFile> {
                throw NotImplementedError("Use copyTo or moveTo")
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
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

    override suspend fun getFiles(path: PhonePath, callback: suspend (Either<Exception, List<PhoneFile>>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = either<Exception, List<PhoneFile>> {
                path.toFile().listFiles()!!.map {
                    PhoneFile(it.toPhonePath())
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
    }

    override suspend fun getFile(path: PhonePath, callback: suspend (Either<Exception, PhoneFile>) -> Unit) {
        val result = either<Exception, PhoneFile> {
            PhoneFile(File(path.fullPath).toPhonePath())
        }

        CoroutineScope(Dispatchers.Main).launch {
            callback(result)
        }
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
    override suspend fun tryLogInAsync(callback: suspend (Either<Exception, PhoneDriver>) -> Unit) {
        callback(Either.Right(PhoneDriver()))
    }
}
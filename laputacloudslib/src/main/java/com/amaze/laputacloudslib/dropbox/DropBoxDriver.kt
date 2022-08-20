package com.amaze.laputacloudslib.dropbox

import arrow.core.Either
import arrow.core.computations.either
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.dropbox.core.v2.DbxClientV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DropBoxDriver(val client: DbxClientV2) : AbstractFileStructureDriver<DropBoxPath, DropBoxFile>() {
    companion object {
        const val SCHEME = "dropbox:"
    }

    override fun getRoot(): DropBoxPath {
        return DropBoxPath("/")
    }

    override suspend fun getFiles(path: DropBoxPath, callback: suspend (Either<Exception, List<DropBoxFile>>) -> Unit) {
        withContext(Dispatchers.IO) {
            val result = either<Exception, List<DropBoxFile>> {
                var listFiles = client.files().listFolder(path.sanitizedPathOrRoot)

                val fileList =
                    mutableListOf<DropBoxFile>()

                while (true) {
                    fileList.addAll(listFiles.entries.map {
                        DropBoxFile(
                            this@DropBoxDriver,
                            DropBoxPath(it.pathLower),
                            false,
                            it.name,
                            it.isFolder()
                        )
                    })

                    if (!listFiles.hasMore) {
                        break
                    }

                    listFiles = client.files().listFolderContinue(listFiles.cursor)
                }

                fileList
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }

    override suspend fun getFile(path: DropBoxPath, callback: suspend (Either<Exception, DropBoxFile>) -> Unit) {
        withContext(Dispatchers.IO) {
            val result = either<Exception, DropBoxFile> {
                if (path.sanitizedPathOrRoot.isNotEmpty()) {//root folder has no metadata
                    val metadata = client.files().getMetadata(path.sanitizedPath)
                    metadata.toDropBoxFile(this@DropBoxDriver)
                } else {
                    DropBoxFile(
                        this@DropBoxDriver,
                        DropBoxPath("/"),
                        true,
                        "root",
                        true
                    )
                }
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }

}
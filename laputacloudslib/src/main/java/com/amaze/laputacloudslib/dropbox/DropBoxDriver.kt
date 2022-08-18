package com.amaze.laputacloudslib.dropbox

import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.amaze.laputacloudslib.CloudPath
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

    override suspend fun getFiles(path: DropBoxPath, callback: suspend (List<DropBoxFile>) -> Unit) {
        withContext(Dispatchers.IO) {
            var result = client.files().listFolder(path.sanitizedPathOrRoot)

            val fileList =
                mutableListOf<DropBoxFile>()

            while (true) {
                fileList.addAll(result.entries.map {
                    DropBoxFile(
                        this@DropBoxDriver,
                        DropBoxPath(it.pathLower),
                        false,
                        it.name,
                        it.isFolder()
                    )
                })

                if (!result.hasMore) {
                    break
                }

                result = client.files().listFolderContinue(result.cursor)
            }

            withContext(Dispatchers.Main) {
                callback(fileList)
            }
        }
    }

    override suspend fun getFile(path: DropBoxPath, callback: suspend (DropBoxFile) -> Unit) {
        withContext(Dispatchers.IO) {
            val file = if (path.sanitizedPathOrRoot.isNotEmpty()) {//root folder has no metadata
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

            withContext(Dispatchers.Main) {
                callback(file)
            }
        }
    }

}
package com.amaze.laputacloudslib.dropbox

import com.amaze.laputacloudslib.DropBoxDriver
import com.amaze.laputacloudslib.DropBoxFile
import com.amaze.laputacloudslib.DropBoxPath
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.Metadata


fun Metadata.isFolder(): Boolean {
    return this is FolderMetadata //as per https://www.dropboxforum.com/t5/API-Support-Feedback/Finding-if-Metadata-is-for-file-or-folder/td-p/167606
}

fun Metadata.toDropBoxFile(driver: DropBoxDriver): DropBoxFile {
    return DropBoxFile(driver, DropBoxPath(pathLower), false, name, isFolder())
}
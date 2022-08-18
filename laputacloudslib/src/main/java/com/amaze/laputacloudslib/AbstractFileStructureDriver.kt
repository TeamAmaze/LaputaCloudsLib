package com.amaze.laputacloudslib

abstract class AbstractFileStructureDriver<Path: CloudPath, File: AbstractCloudFile<Path, File>> {
    abstract fun getRoot(): Path

    abstract suspend fun getFiles(path: Path, callback: suspend (List<File>) -> Unit)

    abstract suspend fun getFile(path: Path, callback: suspend (File) -> Unit)
}
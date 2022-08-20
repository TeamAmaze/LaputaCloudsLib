package com.amaze.laputacloudslib

import arrow.core.Either

abstract class AbstractFileStructureDriver<Path: CloudPath, File: AbstractCloudFile<Path, File>> {
    abstract fun getRoot(): Path

    abstract suspend fun getFiles(path: Path, callback: suspend (Either<Exception, List<File>>) -> Unit)

    abstract suspend fun getFile(path: Path, callback: suspend (Either<Exception, File>) -> Unit)
}
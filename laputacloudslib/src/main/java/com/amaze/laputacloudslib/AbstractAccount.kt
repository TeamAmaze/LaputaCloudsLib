package com.amaze.laputacloudslib

import arrow.core.Either

abstract class AbstractAccount<Path: CloudPath, File: AbstractCloudFile<Path, File>, Driver: AbstractFileStructureDriver<Path, File>> {
    abstract suspend fun tryLogInAsync(callback: suspend (Either<Exception, Driver>) -> Unit)
}

package com.amaze.laputacloudslib

abstract class AbstractAccount<Path: CloudPath, File: AbstractCloudFile<Path, File>, Driver: AbstractFileStructureDriver<Path, File>> {
    abstract suspend fun tryLogInAsync(callback: suspend (Driver) -> Unit)
}

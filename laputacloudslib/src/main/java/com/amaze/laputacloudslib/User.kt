package com.amaze.laputacloudslib

import com.dropbox.core.v2.DbxClientV2

abstract class AbstractUser<Driver: AbstractFileStructureDriver> {
    abstract suspend fun getFileStructureDriverAsync(callback: suspend (Driver) -> Unit)
}

class DropBoxUser(val client: DbxClientV2) : AbstractUser<DropBoxDriver>() {
    override suspend fun getFileStructureDriverAsync(callback: suspend (DropBoxDriver) -> Unit) {
        callback(DropBoxDriver(client))
    }

}
package com.amaze.laputacloudslib

import com.dropbox.core.v2.DbxClientV2

abstract class AbstractUser<Driver: AbstractFileStructureDriver> {
    abstract fun getFileStructureDriverAsync(callback: (Driver) -> Unit)
}

class DropBoxUser(client: DbxClientV2) : AbstractUser<DropBoxDriver>() {
    override fun getFileStructureDriverAsync(callback: (DropBoxDriver) -> Unit) {
        callback(DropBoxDriver())
    }

}
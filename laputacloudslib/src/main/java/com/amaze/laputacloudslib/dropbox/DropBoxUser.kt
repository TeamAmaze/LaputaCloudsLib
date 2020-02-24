package com.amaze.laputacloudslib.dropbox

import com.amaze.laputacloudslib.AbstractUser
import com.dropbox.core.v2.DbxClientV2

class DropBoxUser(val client: DbxClientV2) : AbstractUser<DropBoxDriver>() {
    override suspend fun getFileStructureDriverAsync(callback: suspend (DropBoxDriver) -> Unit) {
        callback(DropBoxDriver(client))
    }

}
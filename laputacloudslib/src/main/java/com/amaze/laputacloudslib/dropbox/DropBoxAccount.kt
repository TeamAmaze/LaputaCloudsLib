package com.amaze.laputacloudslib.dropbox

import com.amaze.laputacloudslib.AbstractAccount
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.amaze.laputacloudslib.AbstractUser
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DropBoxAccount(val accessToken: String) : AbstractAccount() {
    override suspend fun tryLogInAsync(
        callback: suspend (AbstractUser<out AbstractFileStructureDriver>) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            val config =
                DbxRequestConfig.newBuilder("LaputaCloudsLibSample")
                    .build()
            val client = DbxClientV2(config, accessToken)
            callback(DropBoxUser(client))
        }
    }

}
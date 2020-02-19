package com.amaze.laputacloudslib

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


abstract class AbstractAccount {
    abstract suspend fun tryLogInAsync(callback: suspend (AbstractUser<out AbstractFileStructureDriver>) -> Unit)
}

class DropBoxAccount(val accessToken: String) : AbstractAccount() {
    override suspend fun tryLogInAsync(
        callback: suspend (AbstractUser<out AbstractFileStructureDriver>) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            val config = DbxRequestConfig.newBuilder("LaputaCloudsLibSample").build()
            val client = DbxClientV2(config, accessToken)
            callback(DropBoxUser(client))
        }
    }

}
package com.amaze.laputacloudslib

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.microsoft.aad.adal.AuthenticationConstants.OAuth2.ACCESS_TOKEN


abstract class AbstractAccount {
    abstract fun tryLogInAsync(callback: (AbstractUser<out AbstractFileStructureDriver>) -> Unit)
}

class DropBoxAccount : AbstractAccount() {
    override fun tryLogInAsync(callback: (AbstractUser<out AbstractFileStructureDriver>) -> Unit) {
        val config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build()
        val client = DbxClientV2(config, ACCESS_TOKEN)
        callback(DropBoxUser(client))
    }

}
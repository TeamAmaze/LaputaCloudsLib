package com.amaze.laputacloudslib.box

import android.content.Context
import com.amaze.laputacloudslib.AbstractAccount
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.box.androidsdk.content.BoxApiFile
import com.box.androidsdk.content.BoxApiFolder
import com.box.androidsdk.content.BoxConfig
import com.box.androidsdk.content.auth.BoxAuthentication
import com.box.androidsdk.content.models.BoxSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BoxAccount(
    private val context: Context,
    clientId: String,
    clientSecret: String
) : AbstractAccount<BoxPath, BoxFile, BoxDriver>(), BoxAuthentication.AuthListener {

    init {
        BoxConfig.CLIENT_ID = clientId
        BoxConfig.CLIENT_SECRET = clientSecret
    }

    val session = BoxSession(context).also {
        it.setSessionAuthListener(this)
    }

    lateinit var callback: suspend (BoxDriver) -> Unit

    override suspend fun tryLogInAsync(callback: suspend (BoxDriver) -> Unit) {
        this.callback = callback

        session.authenticate(context)
    }

    override fun onLoggedOut(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?) {
        if(info != null && ex != null) {
            throw BoxAccountException("Logged out of Box: " + info.toJson(), ex)
        } else if(info == null && ex != null) {
            throw BoxAccountException("Logged out of Box", ex)
        } else if(info != null && ex == null) {
            throw BoxAccountException("Logged out of Box: " + info.toJson())
        } else if(info == null && ex == null) {
            throw BoxAccountException("Logged out of Box")
        }
    }

    override fun onAuthCreated(info: BoxAuthentication.BoxAuthenticationInfo?) {
        val folderApi = BoxApiFolder(session)
        val fileApi = BoxApiFile(session)

        CoroutineScope(Dispatchers.Main).launch {
            callback(BoxDriver(folderApi, fileApi))
        }
    }

    override fun onRefreshed(info: BoxAuthentication.BoxAuthenticationInfo?) {
        if(info != null) {
            throw BoxAccountException("Connection was restarted: " + info.toJson())
        } else {
            throw BoxAccountException("Connection was restarted")
        }
    }

    override fun onAuthFailure(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?) {
        if(info != null && ex != null) {
            throw BoxAccountException("Auth failure: " + info.toJson(), ex)
        } else if(info == null && ex != null) {
            throw BoxAccountException("Auth failure", ex)
        } else if(info != null && ex == null) {
            throw BoxAccountException("Auth failure: " + info.toJson())
        } else if(info == null && ex == null) {
            throw BoxAccountException("Auth failure")
        }
    }
}
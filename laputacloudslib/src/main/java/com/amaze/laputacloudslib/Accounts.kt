package com.amaze.laputacloudslib

import android.content.Context
import com.amaze.laputacloudslib.googledrive.AuthorizerFragmentData
import com.box.androidsdk.content.BoxApiFile
import com.box.androidsdk.content.BoxApiFolder
import com.box.androidsdk.content.BoxConfig
import com.box.androidsdk.content.auth.BoxAuthentication
import com.box.androidsdk.content.models.BoxSession
import com.google.api.client.auth.oauth2.Credential
import com.google.api.services.drive.Drive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

abstract class AbstractAccount {
    abstract suspend fun tryLogInAsync(callback: suspend (AbstractFileStructureDriver) -> Unit)
}

class BoxAccount(
    private val context: Context,
    clientId: String,
    clientSecret: String
) : AbstractAccount(), BoxAuthentication.AuthListener {

    init {
        BoxConfig.CLIENT_ID = clientId
        BoxConfig.CLIENT_SECRET = clientSecret
    }

    val session = BoxSession(context).also {
        it.setSessionAuthListener(this)
    }

    lateinit var callback: suspend (AbstractFileStructureDriver) -> Unit

    override suspend fun tryLogInAsync(callback: suspend (AbstractFileStructureDriver) -> Unit) {
        this.callback = callback

        session.authenticate(context)
    }

    override fun onLoggedOut(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?) {
        TODO("Not yet implemented")
    }

    override fun onAuthCreated(info: BoxAuthentication.BoxAuthenticationInfo?) {
        val folderApi = BoxApiFolder(session)
        val fileApi = BoxApiFile(session)

        CoroutineScope(Dispatchers.Main).launch {
            callback(BoxDriver(folderApi, fileApi))
        }
    }

    override fun onRefreshed(info: BoxAuthentication.BoxAuthenticationInfo?) {
        TODO("Not yet implemented")
    }

    override fun onAuthFailure(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?) {
        TODO("Not yet implemented")
    }
}


/**
 * Be sure to specify the name of your application. If the application name is {@code null} or
 * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
 */
class GoogleAccount(
    val applicationName: String?,
    val clientId: String,
    val apiKey: String,
    val openInBrowser: (AuthorizerFragmentData) -> Unit
) : AbstractAccount() {
    private val drive: Drive? = null


    override suspend fun tryLogInAsync(callback: suspend (AbstractFileStructureDriver) -> Unit) {
        val onAuthorized = { credential : Credential ->
            //val drive = Drive.Builder(httpTransport, JSON_FACTORY, credential)
            //    .setApplicationName(applicationName)
            //    .build()
        }

        openInBrowser(AuthorizerFragmentData(clientId, apiKey))
    }
}
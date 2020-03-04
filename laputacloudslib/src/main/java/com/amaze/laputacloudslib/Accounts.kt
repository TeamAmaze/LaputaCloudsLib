package com.amaze.laputacloudslib

import com.amaze.laputacloudslib.googledrive.AuthorizerFragmentData
import com.box.sdk.BoxAPIConnection
import com.google.api.client.auth.oauth2.Credential
import com.google.api.services.drive.Drive

abstract class AbstractAccount {
    abstract suspend fun tryLogInAsync(callback: suspend (AbstractFileStructureDriver) -> Unit)
}

class BoxAccount : AbstractAccount {

    val api: BoxAPIConnection

    constructor(clientId: String, clientSecret: String, authCode: String) : super() {
        api = BoxAPIConnection(clientId, clientSecret, authCode)
    }

    constructor(developerToken: String) : super() {
        api = BoxAPIConnection(developerToken)
    }

    override suspend fun tryLogInAsync(callback: suspend (AbstractFileStructureDriver) -> Unit) {
        callback(BoxDriver(api))
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
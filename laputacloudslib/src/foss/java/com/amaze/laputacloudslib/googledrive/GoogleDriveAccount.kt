package com.amaze.laputacloudslib.googledrive

import android.content.Context
import android.content.Intent
import com.amaze.laputacloudslib.AbstractAccount
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.google.api.client.auth.oauth2.Credential
import com.google.api.services.drive.Drive

/**
 * Be sure to specify the name of your application. If the application name is {@code null} or
 * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
 */
class GoogleAccount(
    val context: Context,
    val applicationName: String?,
    val clientId: String,
    val redirectUrl: String
) : AbstractAccount() {
    private val drive: Drive? = null


    override suspend fun tryLogInAsync(callback: suspend (AbstractFileStructureDriver) -> Unit) {
        val onAuthorized = { credential : Credential ->
            //val drive = Drive.Builder(httpTransport, JSON_FACTORY, credential)
            //    .setApplicationName(applicationName)
            //    .build()
        }

        val intent = Intent(context, GoogleDriveOAuthActivity::class.java)
        intent.putExtra(GoogleDriveOAuthActivity.AUTH_DATA, AuthorizerFragmentData(clientId, redirectUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
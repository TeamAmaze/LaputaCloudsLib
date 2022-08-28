package com.amaze.laputacloudslib.googledrive

import android.content.Context
import android.content.Intent
import arrow.core.Either
import arrow.core.computations.either
import com.amaze.laputacloudslib.AbstractAccount
import com.google.api.client.auth.oauth2.Credential
import com.google.api.services.drive.Drive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleAccount(
    val context: Context,
    val applicationName: String?,
    val clientId: String,
    val redirectUrl: String
) : AbstractAccount<GoogleDrivePath, GoogleDriveFile, GoogleDriveDriver>() {
    private val drive: Drive? = null


    override suspend fun tryLogInAsync(callback: suspend (Either<Exception, GoogleDriveDriver>) -> Unit) {
        val onAuthorized = { credential : Credential ->
            CoroutineScope(Dispatchers.Main).launch {
                val result = either<Exception, GoogleDriveDriver> {
                    //val drive = Drive.Builder(httpTransport, JSON_FACTORY, credential)
                    //    .setApplicationName(applicationName)
                    //    .build()
                    TODO("Implement")
                }
                callback(result)
            }
        }

        val intent = Intent(context, GoogleDriveOAuthActivity::class.java)
        intent.putExtra(GoogleDriveOAuthActivity.AUTH_DATA, AuthorizerFragmentData(clientId, redirectUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
package com.amaze.laputacloudslib.googledrive

import android.content.Context
import android.content.Intent
import arrow.core.Either
import com.amaze.laputacloudslib.AbstractAccount
import com.amaze.laputacloudslib.googledrive.GoogleDriveFunctions.getDriveFromAccount
import com.amaze.laputacloudslib.googledrive.GoogleDriveFunctions.getSignInClient
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

internal object GoogleDriveFunctions {
    @JvmStatic
    fun getSignInClient(context: Context): GoogleSignInClient {
        val signInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(DriveScopes.DRIVE))
                .build()
        return GoogleSignIn.getClient(context, signInOptions)
    }

    @JvmStatic
    fun getDriveFromAccount(
        context: Context,
        account: GoogleSignInAccount,
        applicationName: String?
    ): Drive {
        // Use the authenticated account to sign in to the Drive service.
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
            )
            .setApplicationName(applicationName)
            .build()
    }
}

internal object GoogleDriveOnResultCallback {
    var callback: ((GoogleDrivePreSignInActivity, Drive?, ex: Exception?) -> Unit)? = null
}

class GoogleAccount(
    val context: Context,//For foss
    val applicationName: String?,//For foss
    val clientId: String,//For foss
    val redirectUrl: String//For foss
) : AbstractAccount<GoogleDrivePath, GoogleDriveFile, GoogleDriveDriver>() {

    override suspend fun tryLogInAsync(callback: suspend (Either<Exception, GoogleDriveDriver>) -> Unit) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            withContext(Dispatchers.Main) {
                callback(
                    Either.Right(
                        GoogleDriveDriver(
                            getDriveFromAccount(
                                context,
                                account,
                                applicationName
                            )
                        )
                    )
                )
            }
            return
        }

        withContext(Dispatchers.IO) {
            val task: Task<GoogleSignInAccount> = getSignInClient(context).silentSignIn()
            if (task.isSuccessful) {
                // There's immediate result available.
                val signInAccount = task.result ?: throw GoogleDriveException(NullPointerException())
                val driver = GoogleDriveDriver(getDriveFromAccount(context, signInAccount, applicationName))

                withContext(Dispatchers.Main) {
                    callback(Either.Right(driver))
                }
            } else {
                task.addOnSuccessListener { signInAccount: GoogleSignInAccount ->
                    val driver = GoogleDriveDriver(getDriveFromAccount(context, signInAccount, applicationName))

                    MainScope().launch {
                        callback(Either.Right(driver))
                    }
                }

                task.addOnFailureListener { exception: Exception ->
                    if (exception is ApiException
                        && exception.statusCode == GoogleSignInStatusCodes.SIGN_IN_REQUIRED) {
                        loudSignIn(callback)
                        return@addOnFailureListener
                    }

                    MainScope().launch {
                        callback(Either.Left(GoogleDriveException(exception)))
                    }
                }
            }
        }
    }

    private fun loudSignIn(callback: suspend (Either<Exception, GoogleDriveDriver>) -> Unit) {
        GoogleDriveOnResultCallback.callback = { activity: GoogleDrivePreSignInActivity,
                                                 googleDriveService: Drive?,
                                                 ex: Exception? ->
            MainScope().launch {
                if (googleDriveService == null) {
                    if (ex == null) {
                        callback(Either.Left(GoogleDriveException("Something failed when signing in!")))
                    } else {
                        callback(Either.Left(GoogleDriveException(ex)))
                    }
                    return@launch
                }

                GoogleDriveOnResultCallback.callback = null
                callback(Either.Right(GoogleDriveDriver(googleDriveService)))
            }

            context.startActivity(Intent(context, GoogleDrivePreSignInActivity::class.java).also {
                it.putExtra(GoogleDrivePreSignInActivity.APPLICATION_NAME_ARG, applicationName)
            })
        }
    }
}
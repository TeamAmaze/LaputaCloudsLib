package com.amaze.laputacloudslib.googledrive

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.amaze.laputacloudslib.R
import com.amaze.laputacloudslib.googledrive.GoogleDriveFunctions.getDriveFromAccount
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount


class GoogleDrivePreSignInActivity : AppCompatActivity() {

    companion object {
        const val APPLICATION_NAME_ARG = "appname"
    }

    private val REQUEST_CODE_SIGN_IN = 1

    private lateinit var applicationName: String

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_drive_pre_sign_in)

        applicationName = intent.getStringExtra(APPLICATION_NAME_ARG)!!

        progressDialog = ProgressDialog.show(
            this, getString(R.string.app_loading),
            "Loading. Please wait...", true
        )

        val client = GoogleDriveFunctions.getSignInClient(this)

        // The result of the sign-in Intent is handled in onActivityResult.
        ActivityCompat.startActivityForResult(
            this,
            client.signInIntent,
            REQUEST_CODE_SIGN_IN,
            null
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData)
                } else {
                    val exception = GoogleSignIn.getSignedInAccountFromIntent(resultData).exception

                    GoogleDriveOnResultCallback.callback?.invoke(
                        this,
                        null,
                        exception ?: GoogleDriveException("Result code is not RESULT_OK or data is null!")
                    )

                    endActivity()
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData)
    }

    /**
     * Handles the `result` of a completed sign-in activity initiated from [ ][.requestSignIn].
     */
    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                val googleDriveService = getDriveFromAccount(this, googleAccount, applicationName)

                GoogleDriveOnResultCallback.callback?.invoke(this, googleDriveService, null)
                endActivity()
            }
            .addOnFailureListener { exception: Exception? ->
                GoogleDriveOnResultCallback.callback?.invoke(this, null, exception)
                endActivity()
            }
    }

    private fun endActivity() {
        progressDialog.cancel()
        finish()
    }

}

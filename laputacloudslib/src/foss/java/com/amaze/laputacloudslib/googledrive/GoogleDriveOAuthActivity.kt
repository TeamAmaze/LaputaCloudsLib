package com.amaze.laputacloudslib.googledrive

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amaze.laputacloudslib.R

class GoogleDriveOAuthActivity : AppCompatActivity() {
    companion object {
        val AUTH_DATA = "data"
    }

    lateinit var data: AuthorizerFragmentData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_drive_oauth)
        setSupportActionBar(findViewById(R.id.toolbar))

        data = intent.getParcelableExtra(AUTH_DATA) as AuthorizerFragmentData
    }

    override fun onResume() {
        super.onResume()

        startOAuth()
    }

    private fun startOAuth() {
        showSpinner()

        val oauthView = createOAuthView()
        val oauthClient = GoogleDriveOAuthWebViewClient(data.redirectUrl, { code ->

        }, {}, this::dismissSpinner)

        oauthView!!.webViewClient = oauthClient
        oauthView.authenticate(data.clientId, data.redirectUrl)
    }

    private fun showSpinner() {
        //TODO
    }

    private fun dismissSpinner() {
        //TODO
    }

    private fun createOAuthView(): GoogleDriveOAuthWebView? {
        val webview = findViewById<View>(R.id.oauthview) as GoogleDriveOAuthWebView
        webview.visibility = View.VISIBLE
        webview.settings.javaScriptEnabled = true
        webview.settings.saveFormData = false
        webview.settings.savePassword = false
        return webview
    }
}

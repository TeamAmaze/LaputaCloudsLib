package com.amaze.laputacloudslib.googledrive

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.webkit.WebView

class GoogleDriveOAuthWebView(context: Context, attrs: AttributeSet): WebView(context, attrs) {
    val state = GoogleDriveAuth.generateStateToken()

    fun authenticate(clientId: String, redirectUrl: String) {
        val urlBuilder = buildUrl(clientId, redirectUrl)
        urlBuilder.appendQueryParameter("state", state)
        loadUrl(urlBuilder.build().toString())
    }

    protected fun buildUrl(
        clientId: String,
        redirectUrl: String
    ): Uri.Builder {
        // From https://developers.google.com/identity/protocols/oauth2/web-server:
        //https://accounts.google.com/o/oauth2/v2/auth

        val builder = Uri.Builder()
            .scheme("https")
            .authority("accounts.google.com")
            .appendPath("o")
            .appendPath("oauth2")
            .appendPath("v2")
            .appendPath("path")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUrl)
            .appendQueryParameter("response_type", GoogleDriveAuth.RESPONSE_TYPE)
            .appendQueryParameter("scope", GoogleDriveAuth.SCOPE)
        return builder
    }

}
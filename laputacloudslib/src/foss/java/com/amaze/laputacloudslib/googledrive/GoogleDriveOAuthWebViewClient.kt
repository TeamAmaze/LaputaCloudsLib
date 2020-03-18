package com.amaze.laputacloudslib.googledrive

import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.webkit.WebView
import android.webkit.WebViewClient

class GoogleDriveOAuthWebViewClient(
    val redirectUrl: String,
    val onCode: (String) -> Unit,
    val onError: () -> Unit,
    val onFinished: () -> Unit
) : WebViewClient() {
    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        view as GoogleDriveOAuthWebView

        val uri = getURIfromURL(url)
        val code = getValueFromURI(uri, "code")
        if(!TextUtils.isEmpty(code) && code != view.state) {
            onError()
        }

        val error = getValueFromURI(uri, "error")
        if(!TextUtils.isEmpty(error)) {
            onError()
            return
        }

        if(!TextUtils.isEmpty(code)) {
            onCode(code!!)
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onFinished()
    }

    private fun getURIfromURL(url: String): Uri? {
        val uri = Uri.parse(url)
        // In case redirect url is set. We only keep processing if current url matches redirect url.
        if (!TextUtils.isEmpty(redirectUrl)) {
            val redirectUri = Uri.parse(redirectUrl)
            if (redirectUri.scheme == null || redirectUri.scheme != uri.scheme || redirectUri.authority != uri.authority) {
                return null
            }
        }
        return uri
    }

    private fun getValueFromURI(uri: Uri?, key: String): String? {
        if (uri == null) {
            return null
        }
        var value: String? = null
        try {
            value = uri.getQueryParameter(key)
        } catch (e: Exception) {
            // uri cannot be parsed for query param.
        }
        return value
    }
}
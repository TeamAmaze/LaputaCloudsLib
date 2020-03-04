package com.amaze.laputacloudslib.googledrive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.amaze.laputacloudslib.R
import com.google.api.client.auth.oauth2.Credential


class AuthorizerFragment : Fragment() {

    companion object {
        val AUTH_DATA = "data"
    }

    var onAuthorized: ((Credential) -> Unit)? = null

    private lateinit var viewModel: AuthorizerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val data = arguments!!.get(AUTH_DATA) as AuthorizerFragmentData

        val view = inflater.inflate(R.layout.authorizer_fragment, container, false)

        val webView = view.findViewById<WebView>(R.id.webView)
        webView.setWebViewClient(WebViewClient())
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.loadData(getAuthHtml(data.clientId, data.apiKey), "text/html", "UTF-8")

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AuthorizerViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun getAuthHtml(clientId: String, apiKey: String): String {
        requireContext().resources.openRawResource(R.raw.auth).use {
            val b = ByteArray(it.available())
            it.read(b)
            return String(b)
                .replace("<YOUR_CLIENT_ID>", clientId)
                .replace("<YOUR_API_KEY>", apiKey)
        }
    }

}

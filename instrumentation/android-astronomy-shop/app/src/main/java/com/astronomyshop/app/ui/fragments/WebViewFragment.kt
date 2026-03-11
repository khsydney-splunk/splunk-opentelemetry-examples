package com.astronomyshop.app.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.astronomyshop.app.R
import com.splunk.rum.integration.agent.api.SplunkRum
import com.splunk.rum.integration.webview.extension.webViewNativeBridge

class WebViewFragment : Fragment() {

    private lateinit var webView: WebView

    // Replace with your Browser RUM instrumented page
    private val webUrl =
        "https://khsydney-splunk.github.io/splunk-opentelemetry-examples/instrumentation/android-astronomy-shop/brum-test.html"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_webview, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.shopWebView)

        configureWebView(webView)

        // Important: call this BEFORE loadUrl()
        SplunkRum.instance.webViewNativeBridge.integrateWithBrowserRum(webView)

        webView.loadUrl(webUrl)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(webView: WebView) {
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        settings.allowFileAccess = false
        settings.allowContentAccess = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
    }

    override fun onDestroyView() {
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.destroy()
        super.onDestroyView()
    }
}
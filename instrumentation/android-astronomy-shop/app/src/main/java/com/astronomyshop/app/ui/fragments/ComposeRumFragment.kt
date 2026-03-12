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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import com.splunk.rum.integration.agent.api.SplunkRum
import com.splunk.rum.integration.webview.extension.webViewNativeBridge

class ComposeRumFragment : Fragment() {

    private val pages = listOf(
        HybridPage(
            title = "Landing",
            url = "https://khsydney-splunk.github.io/splunk-opentelemetry-examples/instrumentation/android-astronomy-shop/compose-rum-landing.html"
        ),
        HybridPage(
            title = "Catalog",
            url = "https://khsydney-splunk.github.io/splunk-opentelemetry-examples/instrumentation/android-astronomy-shop/compose-rum-catalog.html"
        ),
        HybridPage(
            title = "Checkout",
            url = "https://khsydney-splunk.github.io/splunk-opentelemetry-examples/instrumentation/android-astronomy-shop/compose-rum-checkout.html"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ComposeRumScreen(pages = pages)
                }
            }
        }
    }
}

private data class HybridPage(
    val title: String,
    val url: String
)

@Composable
private fun ComposeRumScreen(pages: List<HybridPage>) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Compose + WebView demo",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        ScrollableTabRow(selectedTabIndex = selectedTab) {
            pages.forEachIndexed { index, page ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(page.title) }
                )
            }
        }

        ComposeWebViewPage(
            page = pages[selectedTab],
            modifier = Modifier.fillMaxSize()
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ComposeWebViewPage(
    page: HybridPage,
    modifier: Modifier = Modifier
) {
    val currentUrl by rememberUpdatedState(page.url)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                configureForRum()

                SplunkRum.instance.webViewNativeBridge.integrateWithBrowserRum(this)

                loadUrl(currentUrl)
            }
        },
        update = { webView ->
            if (webView.url != currentUrl) {
                webView.loadUrl(currentUrl)
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configureForRum() {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.loadsImagesAutomatically = true
    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
    settings.allowFileAccess = false
    settings.allowContentAccess = true
    settings.cacheMode = WebSettings.LOAD_DEFAULT

    webChromeClient = WebChromeClient()
    webViewClient = object : WebViewClient() {
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
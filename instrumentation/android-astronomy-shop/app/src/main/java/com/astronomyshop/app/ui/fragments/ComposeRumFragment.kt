package com.astronomyshop.app.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.splunk.rum.integration.agent.api.SplunkRum
import com.splunk.rum.integration.navigation.extension.navigation
import com.splunk.rum.integration.webview.extension.webViewNativeBridge
import kotlinx.coroutines.flow.collectLatest

class ComposeRumFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    MainNavigationContainer()
                }
            }
        }
    }
}

@Composable
fun MainNavigationContainer() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "landing"

    // 1. COMPOSE UI TRACKING
    // This automatically detects route changes and sends them to Splunk RUM
    TrackNavScreens(navController)

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Compose + WebView RUM",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        // 2. Navigation Tab Bar
        ScrollableTabRow(selectedTabIndex = getIndexFromRoute(currentRoute)) {
            val tabs = listOf("landing", "catalog", "checkout")
            tabs.forEach { route ->
                Tab(
                    selected = currentRoute == route,
                    onClick = {
                        navController.navigate(route) {
                            // Avoid multiple copies of the same destination when reselecting the same tab
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    text = { Text(route.replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        // 3. The NavHost (The "Compose UI" way of swapping screens)
        NavHost(
            navController = navController,
            startDestination = "landing",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("landing") {
                ComposeWebViewPage("https://khsydney-splunk.github.io/splunk-opentelemetry-examples/instrumentation/android-astronomy-shop/compose-rum-landing.html")
            }
            composable("catalog") {
                ComposeWebViewPage("https://khsydney-splunk.github.io/splunk-opentelemetry-examples/instrumentation/android-astronomy-shop/compose-rum-catalog1.html")
            }
            composable("checkout") {
                ComposeWebViewPage("https://khsydney-splunk.github.io/splunk-opentelemetry-examples/instrumentation/android-astronomy-shop/compose-rum-checkout.html")
            }
        }
    }
}

/**
 * Advanced tracking logic that monitors the NavController stream.
 */
@Composable
private fun TrackNavScreens(navController: NavHostController) {
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collectLatest { entry ->
            val route = entry.destination.route ?: "unknown"
            // Sends screen change to Splunk RUM
            SplunkRum.instance.navigation.track("Compose.$route")
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ComposeWebViewPage(url: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                configureForRum()
                // Splunk Browser RUM -> Native RUM Bridge
                SplunkRum.instance.webViewNativeBridge.integrateWithBrowserRum(this)
                loadUrl(url)
            }
        },
        update = { webView ->
            // Only reload if the URL actually changed
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configureForRum() {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        loadsImagesAutomatically = true
        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        allowContentAccess = true
    }
    webChromeClient = WebChromeClient()
    webViewClient = WebViewClient()
}

private fun getIndexFromRoute(route: String): Int {
    return when (route) {
        "landing" -> 0
        "catalog" -> 1
        "checkout" -> 2
        else -> 0
    }
}

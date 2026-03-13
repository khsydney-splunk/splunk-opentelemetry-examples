package com.astronomyshop.app.splunkrum

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.splunk.rum.integration.agent.api.SplunkRum
import androidx.compose.ui.Modifier
import com.splunk.rum.integration.navigation.extension.navigation

@Composable
fun SplunkTrackedNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
//    routeToScreenName: (String) -> String = { it },
    builder: NavGraphBuilder.() -> Unit
) {
    val lastRoute = remember { mutableStateOf<String?>(null) }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route ?: return@OnDestinationChangedListener

            if (lastRoute.value != route) {
                lastRoute.value = route
//                SplunkRum.instance.experimentalSetScreenName(
//                    routeToScreenName(route)
//                )
                SplunkRum.instance.navigation.track("Compose.$route")
            }
        }

        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        builder = builder
    )
}
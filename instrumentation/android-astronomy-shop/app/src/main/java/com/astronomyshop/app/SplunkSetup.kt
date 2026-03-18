//package com.astronomyshop.app
//
//import android.app.Application
//import android.util.Log
//import com.astronomyshop.app.utils.SplunkConfiguration
//import com.splunk.rum.integration.agent.api.AgentConfiguration
//import com.splunk.rum.integration.agent.api.EndpointConfiguration
//import com.splunk.rum.integration.agent.api.SplunkRum
//import com.splunk.rum.integration.agent.common.attributes.MutableAttributes
//import com.splunk.rum.integration.customtracking.extension.customTracking
//import com.splunk.rum.integration.sessionreplay.extension.sessionReplay
//import com.splunk.rum.integration.navigation.NavigationModuleConfiguration
//import io.opentelemetry.api.common.AttributeKey
//import io.opentelemetry.api.trace.Span
//import java.net.HttpURLConnection
//import java.net.URL
//import kotlin.concurrent.thread
//
//
//class SplunkSetup : Application() {
//
//    companion object {
//        private const val TAG = "SplunkSetup"
//
//        fun trackWorkflow(workflowName: String): Span? {
//            return try {
//                SplunkRum.instance.customTracking.trackWorkflow(workflowName)
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to track workflow '$workflowName': ${e.message}")
//                null
//            }
//        }
//
//        fun recordError(
//            errorType: String,
//            errorMessage: String,
//            attributes: Map<String, String> = emptyMap()
//        ) {
//            try {
//                val span = SplunkRum.instance.customTracking.trackWorkflow(errorType)
//                span?.setAttribute(AttributeKey.stringKey("error"), "true")
//                span?.setAttribute(AttributeKey.stringKey("error.type"), errorType)
//                span?.setAttribute(AttributeKey.stringKey("error.message"), errorMessage)
//                attributes.forEach { (key, value) ->
//                    span?.setAttribute(AttributeKey.stringKey(key), value)
//                }
//                span?.addEvent("error_recorded")
//                span?.end()
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to record error: ${e.message}")
//            }
//        }
//
//        fun isConfigurationValid(): Boolean = SplunkConfiguration.isValid
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        initializeSplunkRum()
//    }
//
//    private fun testSplunkReachability() {
//        thread {
//            try {
//                val url = URL("https://rum-ingest.${SplunkConfiguration.REALM}.signalfx.com")
//                val conn = (url.openConnection() as HttpURLConnection).apply {
//                    requestMethod = "GET"
//                    connectTimeout = 8000
//                    readTimeout = 8000
//                }
//                conn.connect()
//                Log.i("SplunkNetTest", "Reachability OK. HTTP=${conn.responseCode}")
//                conn.disconnect()
//            } catch (e: Exception) {
//                Log.e("SplunkNetTest", "Reachability FAILED: ${e.javaClass.simpleName}: ${e.message}")
//            }
//        }
//    }
//
//    private fun initializeSplunkRum() {
//        if (!SplunkConfiguration.isValid) {
//            Log.w(TAG, "Update REALM and TOKEN in SplunkConfiguration.kt")
//            return
//        }
//
//        try {
//            val globalAttributes = MutableAttributes().apply {
//                this["app.name"] = SplunkConfiguration.APP_NAME //Sample Only, Create your own global attributes
//                this["app.state"] = "LocalRun"
//            }
//
//            SplunkRum.install(
//                application = this,
//                agentConfiguration = AgentConfiguration(
//                    endpoint = EndpointConfiguration(
//                        realm = SplunkConfiguration.REALM,
//                        rumAccessToken = SplunkConfiguration.TOKEN
//                    ),
//                    appName = SplunkConfiguration.APP_NAME,
//                    appVersion = SplunkConfiguration.MOBILE_APP_VERSION,
//                    deploymentEnvironment = SplunkConfiguration.ENVIRONMENT,
//                    globalAttributes = globalAttributes
//                ),
//                moduleConfigurations = arrayOf(
//                    // Tracks navigation for activities and fragments.
//                    // isEnabled = true to enable navigation tracking.
//                    // isAutomatedTrackingEnabled = false disables automated tracking.
//                    NavigationModuleConfiguration(
//                        isEnabled = true,
//                        isAutomatedTrackingEnabled = true
//                    )
//                )
//
//
//            )
//
//            SplunkRum.instance.sessionReplay.start()
//
//            testSplunkReachability()
//
//            Log.i(TAG, "Splunk RUM initialized — realm=${SplunkConfiguration.REALM}, env=${SplunkConfiguration.ENVIRONMENT}")
//        } catch (e: Exception) {
//            Log.e(TAG, "RUM installation failed: ${e.message}")
//        }
//    }
//}
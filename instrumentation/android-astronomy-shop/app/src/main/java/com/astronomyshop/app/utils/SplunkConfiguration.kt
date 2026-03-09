package com.astronomyshop.app.utils

object SplunkConfiguration {

    // Replace with your Splunk realm and RUM access token
    const val REALM = "us1"
    const val TOKEN = "your_RUM_token"

    const val ENVIRONMENT = "test"
    const val MOBILE_APP_VERSION = "1.0.0"
    const val APP_NAME = "Application Name"

    val ENDPOINT_URL = "https://rum-ingest.$REALM.signalfx.com/v1/rum"

    val isValid: Boolean
        get() = TOKEN.isNotEmpty()
                && !TOKEN.contains("YOUR_")
                && !TOKEN.contains("_TOKEN_HERE")
                && REALM.isNotEmpty()
                && !REALM.contains("YOUR_")
}

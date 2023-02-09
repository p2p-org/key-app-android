package org.p2p.wallet.sdk.facade

import org.p2p.wallet.BuildConfig
import timber.log.Timber

private const val TAG = "AppSdkLogger"

class AppSdkLogger {

    fun logRequest(methodName: String, vararg params: Any?) {
        val logText = buildString {
            append(methodName)
            append(" ---------->")
            if (BuildConfig.DEBUG && params.isNotEmpty()) {
                appendLine()
                append("(")
                appendLine()
                append(params.withIndex().joinToString(separator = "") { "param ${it.index}: ${it.value}\n" })
                append(")")
            }
        }
        Timber.tag(TAG).i(logText)
    }

    fun logResponse(methodName: String, response: String) {
        val logText = buildString {
            append(methodName)
            append("<----------")
            if (BuildConfig.DEBUG) {
                appendLine()
                append(response)
            }
        }
        Timber.tag(TAG).i(logText)
    }
}

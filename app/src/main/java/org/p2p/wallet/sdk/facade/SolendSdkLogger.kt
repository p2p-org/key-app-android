package org.p2p.wallet.sdk.facade

import org.p2p.wallet.BuildConfig
import timber.log.Timber

private const val TAG = "SolendSdkLogger"

class SolendSdkLogger {

    fun logRequest(methodName: String, vararg params: Any?) {
        val logText = buildString {
            append(methodName)
            append(" ---------->")
            if (BuildConfig.DEBUG && params.isNotEmpty()) {
                appendLine()
                append("(")
                appendLine()
                append(params.withIndex().joinToString { "param ${it.index}: ${it.value}\n" })
                append(")")
            }
        }
        Timber.tag(TAG).i(logText)
    }
}

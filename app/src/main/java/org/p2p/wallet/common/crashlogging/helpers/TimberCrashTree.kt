package org.p2p.wallet.common.crashlogging.helpers

import android.util.Log
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.crashlogging.CrashLogger
import org.p2p.wallet.updates.NetworkConnectionStateProvider

class TimberCrashTree(
    private val crashLogger: CrashLogger,
    private val networkProvider: NetworkConnectionStateProvider,
) : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.INFO && super.isLoggable(tag, priority)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val isThrowablePriority = priority == Log.ERROR
        val isInformationPriority = priority == Log.WARN || priority == Log.INFO
        val priorityAsString = priority.priorityToString()
        if (BuildConfig.DEBUG && priority == Log.DEBUG) {
            crashLogger.logInformation("[$tag] [$priorityAsString] $message")
        }

        val isNetworkError = t is SocketTimeoutException || t is UnknownHostException
        val userHasNoInternet = !networkProvider.hasConnection()
        if (isNetworkError && userHasNoInternet && isThrowablePriority) {
            // log as info, do not create entry in Sentry or Crashlytics
            log(Log.INFO, tag, message, t)
        }

        when {
            isThrowablePriority && t != null -> {
                crashLogger.logThrowable(t, message)
            }
            isThrowablePriority && t == null -> {
                crashLogger.logInformation("[$tag] [$priorityAsString] $message")
            }
            isInformationPriority && t != null -> {
                crashLogger.logInformation("[$tag] [$priorityAsString] ($message): $t")
            }
            isInformationPriority -> {
                crashLogger.logInformation("[$tag] [$priorityAsString] $message")
            }
        }
    }

    private fun Int.priorityToString(): String {
        return when (this) {
            Log.INFO -> "INFO"
            Log.WARN -> "WARN"
            Log.ERROR -> "ERROR"
            Log.VERBOSE -> "VERBOSE"
            Log.ASSERT -> "ASSERT"
            Log.DEBUG -> "DEBUG"
            else -> "UNKNOWN"
        }
    }
}

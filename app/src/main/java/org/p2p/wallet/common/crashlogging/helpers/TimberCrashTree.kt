package org.p2p.wallet.common.crashlogging.helpers

import android.util.Log
import timber.log.Timber
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.crashlogging.CrashLogger

class TimberCrashTree(
    private val crashLogger: CrashLogger
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
            else -> {
                t?.also { crashLogger.logInformation("[$tag] [$priorityAsString] ${t.stackTraceToString()}") }
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

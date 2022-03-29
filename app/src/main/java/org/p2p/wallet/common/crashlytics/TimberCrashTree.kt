package org.p2p.wallet.common.crashlytics

import android.util.Log
import timber.log.Timber

class TimberCrashTree(
    private val crashLoggingService: CrashLoggingService
) : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.INFO && super.isLoggable(tag, priority)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val isThrowablePriority = priority == Log.ERROR
        val isInformationPriority = priority == Log.WARN || priority == Log.INFO
        val priorityAsString = priority.priorityToString()
        when {
            isThrowablePriority && t != null -> {
                crashLoggingService.logThrowable(t)
            }
            isThrowablePriority && t == null -> {
                crashLoggingService.logInformation("[$priorityAsString] $message")
            }
            isInformationPriority && t != null -> {
                crashLoggingService.logInformation("[$priorityAsString] $t ($message)")
            }
            isInformationPriority -> {
                crashLoggingService.logInformation("[$priorityAsString] $message")
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

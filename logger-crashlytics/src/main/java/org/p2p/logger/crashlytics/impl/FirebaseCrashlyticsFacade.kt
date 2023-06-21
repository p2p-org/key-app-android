package org.p2p.logger.crashlytics.impl

import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.p2p.logger.crashlytics.CrashLoggingFacade

@Suppress("DEPRECATION")
class FirebaseCrashlyticsFacade : CrashLoggingFacade {
    init {
        crashlytics.setCrashlyticsCollectionEnabled(true)
    }

    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    override fun logInformation(information: String) {
        crashlytics.log(information)
    }

    override fun logThrowable(error: Throwable, message: String?) {
        if (message != null) {
            logInformation("${error.javaClass.name}: $message")
        }
        crashlytics.recordException(error)
    }

    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    override fun clearUserId() {
        crashlytics.setUserId("-")
    }

    override fun setCustomKey(key: String, value: Any) {
        with(crashlytics) {
            when (value) {
                is Int -> setCustomKey(key, value)
                is String -> setCustomKey(key, value)
                is Boolean -> setCustomKey(key, value)
                is Float -> setCustomKey(key, value)
                is Double -> setCustomKey(key, value)
                else -> setCustomKey(key, value.toString())
            }
        }
    }
}

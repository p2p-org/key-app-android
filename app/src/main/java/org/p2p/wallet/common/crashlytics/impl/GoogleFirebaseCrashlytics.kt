package org.p2p.wallet.common.crashlytics.impl

import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.p2p.wallet.common.crashlytics.CrashLoggingService

class GoogleFirebaseCrashlytics : CrashLoggingService {

    override var isLoggingEnabled: Boolean = false
        set(value) {
            field = value
            crashlytics.setCrashlyticsCollectionEnabled(value)
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

    override fun setUserId(userId: CrashLoggingService.UserId) {
        when (userId) {
            is CrashLoggingService.UserId.Filled -> crashlytics.setUserId(userId.value)
            is CrashLoggingService.UserId.NotSet -> crashlytics.setUserId("-")
        }
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

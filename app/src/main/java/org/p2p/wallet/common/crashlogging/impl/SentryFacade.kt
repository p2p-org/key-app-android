package org.p2p.wallet.common.crashlogging.impl

import io.sentry.Sentry
import org.p2p.wallet.common.crashlogging.CrashLoggingFacade

/**
 * Disable mechanism is located in build.gradle
 * see build.gradle sentry { ... } block
 */
class SentryFacade : CrashLoggingFacade {

    override fun logInformation(information: String) {
        Sentry.addBreadcrumb(information)
    }

    override fun logThrowable(error: Throwable, message: String?) {
        if (message != null) {
            Sentry.addBreadcrumb("${error.javaClass.name}: $message")
        }
        Sentry.captureException(error)
    }

    override fun setUserId(userId: String) {
        Sentry.setUser(io.sentry.protocol.User().apply { id = userId })
    }

    override fun clearUserId() {
        Sentry.setUser(null)
    }

    override fun setCustomKey(key: String, value: Any) {
        Sentry.setExtra(key, value.toString())
    }
}

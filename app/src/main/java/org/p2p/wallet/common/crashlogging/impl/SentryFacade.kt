@file:Suppress("DEPRECATION")
package org.p2p.wallet.common.crashlogging.impl

import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import org.p2p.wallet.common.crashlogging.CrashLoggingFacade

/**
 * Disable mechanism is located in build.gradle
 * see build.gradle sentry { ... } block
 */
private const val BREADCRUMB_CATEGORY = "SentryFacade"

class SentryFacade : CrashLoggingFacade {

    override fun logInformation(information: String) {
        Sentry.addBreadcrumb(
            Breadcrumb(information).apply {
                category = BREADCRUMB_CATEGORY
                level = SentryLevel.INFO
            }
        )
    }

    override fun logThrowable(error: Throwable, message: String?) {
        if (message != null) {
            Sentry.addBreadcrumb(
                Breadcrumb("${error.javaClass.name}: $message").apply {
                    category = BREADCRUMB_CATEGORY
                    level = SentryLevel.ERROR
                }
            )
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
        val validatedValue: String = if (value is String) {
            value.takeIf(String::isNotBlank) ?: "-"
        } else {
            value.toString()
        }
        Sentry.setExtra(key, validatedValue)
    }
}

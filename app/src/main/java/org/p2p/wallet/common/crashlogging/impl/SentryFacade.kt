@file:Suppress("DEPRECATION")
package org.p2p.wallet.common.crashlogging.impl

import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.protocol.User
import org.p2p.wallet.common.crashlogging.CrashLoggingFacade

private const val BREADCRUMB_CATEGORY = "SentryFacade"

class SentryFacade : CrashLoggingFacade {

    private fun createBreadcrumb(
        message: String,
        level: SentryLevel = SentryLevel.INFO
    ): Breadcrumb = Breadcrumb(message).apply {
        this.category = BREADCRUMB_CATEGORY
        this.level = level
    }

    override fun logInformation(information: String) {
        Sentry.addBreadcrumb(createBreadcrumb(information))
    }

    override fun logThrowable(error: Throwable, message: String?) {
        if (message != null) {
            Sentry.addBreadcrumb(
                createBreadcrumb("${error.javaClass.name}: $message", SentryLevel.ERROR)
            )
        }
        Sentry.captureException(error)
    }

    override fun setUserId(userId: String) {
        Sentry.setUser(User().apply { id = userId })
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

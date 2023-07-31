package org.p2p.wallet.auth.analytics

import org.p2p.core.analytics.Analytics
import org.p2p.core.analytics.constants.EventNames.AUTH_RESET_INVOKED
import org.p2p.core.analytics.constants.EventNames.AUTH_RESET_VALIDATED
import org.p2p.core.analytics.constants.EventNames.AUTH_VALIDATED
import org.p2p.core.analytics.constants.EventNames.AUTH_VIEWED

class AuthAnalytics(private val tracker: Analytics) {

    fun logAuthViewed(lastScreenName: String, authType: AuthType) {
        tracker.logEvent(
            AUTH_VIEWED,
            arrayOf(
                Pair("Auth_Source", lastScreenName),
                Pair("Auth_Type", authType.title)
            )
        )
    }

    fun logAuthValidated(result: AuthResult, authType: AuthType) {
        tracker.logEvent(
            AUTH_VALIDATED,
            arrayOf(
                Pair("Auth_Result", result.title),
                Pair("Auth_Type", authType.title)
            )
        )
    }

    fun logAuthResetInvoked() {
        tracker.logEvent(AUTH_RESET_INVOKED)
    }

    fun logAuthResetValidated(result: ResetResult) {
        tracker.logEvent(
            AUTH_RESET_VALIDATED,
            arrayOf(
                Pair("Auth_Reset_Result", result.title)
            )
        )
    }

    enum class AuthType(val title: String) {
        PIN("PIN"),
        PASSWORD("Password"),
        BIOMETRIC("Biometric")
    }

    enum class AuthResult(val title: String) {
        SUCCESS("Success"),
        ERROR("Error"),
        BLOCKED("Blocked")
    }

    enum class ResetResult(val title: String) {
        SUCCESS("Success"),
        ERROR("Error")
    }
}

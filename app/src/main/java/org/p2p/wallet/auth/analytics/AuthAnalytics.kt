package org.p2p.wallet.auth.analytics

import org.p2p.wallet.common.analytics.TrackerContract

class AuthAnalytics(private val tracker: TrackerContract) {

    fun logAuthViewed(lastScreenName: String, authType: AuthType) {
        tracker.logEvent(
            "Auth_Viewed",
            arrayOf(
                Pair("Auth_Source", lastScreenName),
                Pair("Auth_Type", authType.title)
            )
        )
    }

    fun logAuthValidated(result: AuthResult, authType: AuthType) {
        tracker.logEvent(
            "Auth_Validated",
            arrayOf(
                Pair("Auth_Result", result.title),
                Pair("Auth_Type", authType.title)
            )
        )
    }

    fun logAuthResetInvoked() {
        tracker.logEvent("Auth_Reset_Invoked")
    }

    fun logAuthResetValidated(result: ResetResult) {
        tracker.logEvent(
            "Auth_Reset_Validated",
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
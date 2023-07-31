package org.p2p.wallet.auth.analytics

import org.p2p.core.analytics.Analytics

private const val USERNAME_CREATION_BUTTON = "Username_Creation_Button"
private const val USERNAME_SKIP_BUTTON = "Username_Skip_Button"
private const val USERNAME_CREATION_SCREEN = "Username_Creation_Screen"

class UsernameAnalytics(private val tracker: Analytics) {
    fun logCreateUsernameClicked() {
        tracker.logEvent(
            event = USERNAME_CREATION_BUTTON,
            params = mapOf(
                "Result" to "success"
            )
        )
    }

    fun logSkipUsernameClicked() {
        tracker.logEvent(
            event = USERNAME_SKIP_BUTTON,
            params = mapOf(
                "Result" to "success"
            )
        )
    }

    fun logUsernameCreationScreenOpened() {
        tracker.logEvent(event = USERNAME_CREATION_SCREEN)
    }
}

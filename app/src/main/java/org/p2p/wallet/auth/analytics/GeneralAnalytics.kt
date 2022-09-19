package org.p2p.wallet.auth.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames

class GeneralAnalytics(private val tracker: Analytics) {
    fun logActionButtonClicked() {
        tracker.logEvent(
            event = EventNames.GENERAL_ACTION_BUTTON_CLICKED
        )
    }
}

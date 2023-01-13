package org.p2p.wallet.auth.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.sell.interactor.SellInteractor
import kotlinx.coroutines.launch

class GeneralAnalytics(
    private val tracker: Analytics,
    private val sellInteractor: SellInteractor,
    private val appScope: AppScope
) {
    fun logActionButtonClicked(lastScreenName: String) {
        appScope.launch {
            tracker.logEvent(
                event = EventNames.GENERAL_ACTION_BUTTON_CLICKED,
                params = mapOf(
                    "Last_Screen" to lastScreenName,
                    "isSellEnabled" to sellInteractor.isSellAvailable()
                )
            )
        }
    }
}

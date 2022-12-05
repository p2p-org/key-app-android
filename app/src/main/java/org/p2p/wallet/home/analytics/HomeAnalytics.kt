package org.p2p.wallet.home.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.HOME_USER_AGGREGATE_BALANCE
import org.p2p.wallet.common.analytics.constants.EventNames.HOME_USER_HAS_POSITIVE_BALANCE
import java.math.BigDecimal

class HomeAnalytics(private val tracker: Analytics) {

    fun logUserHasPositiveBalanceProperty(hasPositiveBalance: Boolean) {
        tracker.setUserPropertyOnce(HOME_USER_HAS_POSITIVE_BALANCE, hasPositiveBalance)
        logUserHasPositiveBalanceEvent(hasPositiveBalance)
    }

    fun logUserAggregateBalanceProperty(usdBalance: BigDecimal) {
        tracker.setUserPropertyOnce(HOME_USER_AGGREGATE_BALANCE, usdBalance.toString())
        logUserAggregateBalanceEvent(usdBalance)
    }

    private fun logUserAggregateBalanceEvent(usdBalance: BigDecimal) {
        tracker.logEvent(
            HOME_USER_AGGREGATE_BALANCE,
            arrayOf("Usd_Balance" to usdBalance)
        )
    }

    private fun logUserHasPositiveBalanceEvent(hasPositiveBalance: Boolean) {
        tracker.logEvent(
            HOME_USER_HAS_POSITIVE_BALANCE,
            arrayOf("Has_Positive_Balance" to hasPositiveBalance)
        )
    }
}

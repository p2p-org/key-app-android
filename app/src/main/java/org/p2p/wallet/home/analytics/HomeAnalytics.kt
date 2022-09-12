package org.p2p.wallet.home.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.HOME_USER_AGGREGATE_BALANCE
import org.p2p.wallet.common.analytics.constants.EventNames.HOME_USER_HAS_POSITIVE_BALANCE
import java.math.BigDecimal

class HomeAnalytics(private val tracker: Analytics) {

    fun logUserHasPositiveBalance(hasPositiveBalance: Boolean) {
        tracker.logEvent(
            HOME_USER_HAS_POSITIVE_BALANCE,
            arrayOf("User_Has_Positive_Balance" to hasPositiveBalance)
        )
    }

    fun logUserAggregateBalance(usdBalance: BigDecimal) {
        tracker.logEvent(
            HOME_USER_AGGREGATE_BALANCE,
            arrayOf("User_Aggregate_Balance" to usdBalance)
        )
    }
}

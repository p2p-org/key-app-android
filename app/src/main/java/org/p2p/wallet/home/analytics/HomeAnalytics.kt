package org.p2p.wallet.home.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.HOME_USER_AGGREGATE_BALANCE
import org.p2p.wallet.common.analytics.constants.EventNames.HOME_USER_HAS_POSITIVE_BALANCE
import java.math.BigDecimal

class HomeAnalytics(private val tracker: Analytics) {

    fun logUserHasPositiveBalanceProperty(hasPositiveBalance: Boolean) {
        tracker.setUserProperty(HOME_USER_HAS_POSITIVE_BALANCE, hasPositiveBalance)
    }

    fun logUserAggregateBalanceProperty(usdBalance: BigDecimal) {
        tracker.setUserProperty(HOME_USER_AGGREGATE_BALANCE, usdBalance.toString())
    }
}

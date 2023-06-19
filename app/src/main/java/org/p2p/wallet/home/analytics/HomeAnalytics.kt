package org.p2p.wallet.home.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.HOME_USER_AGGREGATE_BALANCE
import org.p2p.wallet.common.analytics.constants.EventNames.HOME_USER_HAS_POSITIVE_BALANCE
import java.math.BigDecimal

private const val TOKEN_DETAILS_CLICKED = "Main_Screen_Token_Details_Open"

class HomeAnalytics(private val tracker: Analytics) {

    fun logUserHasPositiveBalanceProperty(hasPositiveBalance: Boolean) {
        tracker.setUserPropertyOnce(HOME_USER_HAS_POSITIVE_BALANCE, hasPositiveBalance)
        logUserHasPositiveBalanceEvent(hasPositiveBalance)
    }

    fun logUserAggregateBalanceProperty(usdBalance: BigDecimal) {
        tracker.setUserPropertyOnce(HOME_USER_AGGREGATE_BALANCE, usdBalance.toString())
        logUserAggregateBalanceEvent(usdBalance)
    }

    fun logMainScreenTokenDetailsOpen(tokenTier: String) {
        tracker.logEvent(
            event = TOKEN_DETAILS_CLICKED,
            params = arrayOf(
                "Token_Tier" to tokenTier
            )
        )
    }

    fun logHiddenTokensClicked() {
        tracker.logEvent("Main_Screen_Hidden_Tokens")
    }

    fun logBottomNavigationHomeClicked() {
        tracker.logEvent("Main_Wallet")
    }

    fun logBottomNavigationEarnClicked() {
        tracker.logEvent("Main_Earn")
    }

    fun logBottomNavigationHistoryClicked() {
        tracker.logEvent("Main_History")
    }

    fun logBottomNavigationSettingsClicked() {
        tracker.logEvent("Main_Settings")
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

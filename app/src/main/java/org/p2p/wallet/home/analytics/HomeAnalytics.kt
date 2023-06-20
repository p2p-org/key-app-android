package org.p2p.wallet.home.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.HOME_USER_AGGREGATE_BALANCE
import org.p2p.wallet.common.analytics.constants.EventNames.HOME_USER_HAS_POSITIVE_BALANCE
import java.math.BigDecimal

private const val TOKEN_DETAILS_CLICKED = "Main_Screen_Token_Details_Open"
private const val HOME_MAIN_WALLET = "Main_Wallet"
private const val HOME_MAIN_EARN = "Main_Earn"
private const val HOME_MAIN_HISTORY = "Main_History"
private const val HOME_MAIN_SETTINGS = "Main_Settings"
private const val HOME_HIDDEN_TOKENS_CLICKED = "Main_Screen_Hidden_Tokens"

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
            params = mapOf(
                "Token_Tier" to tokenTier
            )
        )
    }

    fun logHiddenTokensClicked() {
        tracker.logEvent(HOME_HIDDEN_TOKENS_CLICKED)
    }

    fun logBottomNavigationHomeClicked() {
        tracker.logEvent(event = HOME_MAIN_WALLET)
    }

    fun logBottomNavigationEarnClicked() {
        tracker.logEvent(event = HOME_MAIN_EARN)
    }

    fun logBottomNavigationHistoryClicked() {
        tracker.logEvent(HOME_MAIN_HISTORY)
    }

    fun logBottomNavigationSettingsClicked() {
        tracker.logEvent(HOME_MAIN_SETTINGS)
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

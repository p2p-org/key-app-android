package org.p2p.wallet.home.ui.wallet.analytics

import java.math.BigDecimal
import org.p2p.core.model.CurrencyMode
import org.p2p.core.utils.isMoreThan
import org.p2p.core.analytics.Analytics
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor

private const val MAIN_SCREEN_OPENED = "Main_Screen_Opened"
private const val MAIN_SCREEN_ADDRESS_CLICK = "Main_Screen_Address_Click"
private const val MAIN_SCREEN_AMOUNT_CLICK = "Main_Screen_Amount_Click"
private const val MAIN_SCREEN_ADD_MONEY_CLICK = "Main_Screen_Add_Money_Click"
private const val MAIN_SCREEN_WITHDRAW_CLICK = "Main_Screen_Withdraw_Click"

private const val MAIN_SCREEN_MAIN_CLICK = "Main_Screen_Main_Click"
private const val MAIN_SCREEN_CRYPTO_CLICK = "Main_Screen_Crypto_Click"
private const val MAIN_SCREEN_SEND_CLICK = "Main_Screen_Send_Click"
private const val MAIN_SCREEN_HISTORY_CLICK = "Main_Screen_History_Click"
private const val MAIN_SCREEN_SETTINGS_CLICK = "Main_Screen_Settings_Click"

private const val MAIN_SCREEN_CLAIM_TRANSFERED_VIEWED = "Main_Screen_Claim_Transfered_Viewed"
private const val MAIN_SCREEN_CLAIM_TRANSFERED_CLICK = "Main_Screen_Claim_Transfered_Click"

private const val USER_AGGREGATE_BALANCE_BASE = "User_Aggregate_Balance_Base"
private const val USER_HAS_POSITIVE_BALANCE_BASE = "User_Has_Positive_Balance_Base"

class MainScreenAnalytics(
    private val tracker: Analytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor
) {

    fun logMainScreenOpen(isSellEnabled: Boolean) {
        tracker.logEvent(
            event = MAIN_SCREEN_OPENED,
            params = mapOf(
                "Last_Screen" to analyticsInteractor.getPreviousScreenName(),
                "Is_Sell_Enabled" to isSellEnabled
            ),
        )
    }

    fun logMainScreenAddressClick() {
        tracker.logEvent(event = MAIN_SCREEN_ADDRESS_CLICK)
    }

    fun logMainScreenAmountClick() {
        tracker.logEvent(event = MAIN_SCREEN_AMOUNT_CLICK)
    }

    fun logMainScreenAddMoneyClick() {
        tracker.logEvent(event = MAIN_SCREEN_ADD_MONEY_CLICK)
    }

    fun logMainScreenWithdrawClick() {
        tracker.logEvent(event = MAIN_SCREEN_WITHDRAW_CLICK)
    }

    fun logMainScreenMainClick() {
        tracker.logEvent(event = MAIN_SCREEN_MAIN_CLICK)
    }

    fun logMainScreenCryptoClick() {
        tracker.logEvent(event = MAIN_SCREEN_CRYPTO_CLICK)
    }

    fun logMainScreenSendClick() {
        tracker.logEvent(event = MAIN_SCREEN_SEND_CLICK)
    }

    fun logMainScreenHistoryClick() {
        tracker.logEvent(event = MAIN_SCREEN_HISTORY_CLICK)
    }

    fun logMainScreenSettingsClick() {
        tracker.logEvent(event = MAIN_SCREEN_SETTINGS_CLICK)
    }

    fun logMainScreenClaimTransferedViewed(claimCount: Int) {
        tracker.logEvent(
            event = MAIN_SCREEN_CLAIM_TRANSFERED_VIEWED,
            params = mapOf(
                "Claim_Count" to claimCount,
            ),
        )
    }

    fun logMainScreenClaimTransferedClick() {
        tracker.logEvent(event = MAIN_SCREEN_CLAIM_TRANSFERED_CLICK)
    }

    fun logUserAggregateBalanceBase(balanceAmount: BigDecimal) {
        val amountInUsd = balanceAmount // TODO convert to Usd even if it will be euro.. etc
        tracker.logEvent(
            event = USER_AGGREGATE_BALANCE_BASE,
            params = mapOf(
                "Amount_USD" to amountInUsd,
                "Currency" to CurrencyMode.Fiat.Usd.fiatAbbreviation,
            ),
        )
        val hasPositiveBalance = balanceAmount.isMoreThan(BigDecimal.ZERO)
        logUserHasPositiveBalanceBase(hasPositiveBalance)
    }

    private fun logUserHasPositiveBalanceBase(isNotZero: Boolean) {
        tracker.logEvent(
            event = USER_HAS_POSITIVE_BALANCE_BASE,
            params = mapOf(
                "State" to isNotZero,
            ),
        )
    }
}

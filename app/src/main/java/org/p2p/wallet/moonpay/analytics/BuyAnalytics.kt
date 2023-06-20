package org.p2p.wallet.moonpay.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_BUTTON_PRESSED
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_CHOSEN_METHOD_PAYMENT
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_COIN_CHANGED
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_CONTINUING
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_CURRENCY_CHANGED
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_GOING_BACK
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_MOONPAY_WINDOW_OPENED
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_PAYMENT_RESULT_SHOWN
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_SCREEN_OPENED
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_TOKEN_CHOSEN
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_TOTAL_SHOWED
import org.p2p.wallet.moonpay.model.PaymentMethod
import java.math.BigDecimal

private const val TOPUP_HOME_BAR_BUTTON = "Main_Screen_Topup_Bar"
private const val TOPUP_ACTION_BUTTON_CLICKED = "Action_Button_Buy"
private const val BUY_TOKEN_SCREEN_ACTION_CLICKED = "Token_Screen_Buy_Bar"

class BuyAnalytics(private val tracker: Analytics) {

    fun logTokenScreenActionClicked() {
        tracker.logEvent(BUY_TOKEN_SCREEN_ACTION_CLICKED)
    }

    fun logTopupHomeBarClicked() {
        tracker.logEvent(TOPUP_HOME_BAR_BUTTON)
    }

    fun logTopupActionButtonClicked() {
        tracker.logEvent(TOPUP_ACTION_BUTTON_CLICKED)
    }

    fun logScreenOpened(lastScreenName: String) {
        tracker.logEvent(
            BUY_SCREEN_OPENED,
            arrayOf(
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logBuyTokenChosen(tokenName: String, lastScreenName: String) {
        tracker.logEvent(
            BUY_TOKEN_CHOSEN,
            arrayOf(
                Pair("Token_Name", tokenName),
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logBuyGoingBack(buySum: BigDecimal, buyCurrency: String, buyUSD: BigDecimal) {
        tracker.logEvent(
            BUY_GOING_BACK,
            arrayOf(
                Pair("Buy_Sum", buySum),
                Pair("Buy_Currency", buyCurrency),
                Pair("Buy_USD", buyUSD)
            )
        )
    }

    fun logBuyContinuing(
        buySum: BigDecimal,
        buyCurrency: String,
        buyProvider: String,
        buyUSD: BigDecimal,
        lastScreenName: String
    ) {
        tracker.logEvent(
            BUY_CONTINUING,
            arrayOf(
                Pair("Buy_Sum", buySum),
                Pair("Buy_Currency", buyCurrency),
                Pair("Buy_Provider", buyProvider),
                Pair("Buy_USD", buyUSD),
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logBuyPaymentResultShown(result: BuyResult) {
        tracker.logEvent(
            BUY_PAYMENT_RESULT_SHOWN,
            arrayOf(
                Pair("Buy_Result", result.title)
            )
        )
    }

    fun logBuyCurrencyChanged(fromCurrency: String, toCurrency: String) {
        tracker.logEvent(
            BUY_CURRENCY_CHANGED,
            mapOf(
                "From_Currency" to fromCurrency,
                "To_Currency" to toCurrency
            )
        )
    }

    fun logBuyTokenChanged(fromToken: String, toToken: String) {
        tracker.logEvent(
            BUY_COIN_CHANGED,
            mapOf(
                "From_Coin" to fromToken,
                "To_Coin" to toToken
            )
        )
    }

    fun logBuyMethodPaymentChanged(methodPayment: PaymentMethod) {
        tracker.logEvent(
            BUY_CHOSEN_METHOD_PAYMENT,
            mapOf("Type" to methodPayment.toType())
        )
    }

    fun logBuyButtonPressed(
        buySumCurrency: String,
        buySumCoin: String,
        buyCurrency: String,
        buyCoin: String,
        methodPayment: PaymentMethod?
    ) {
        val isBankTransfer = methodPayment?.method == PaymentMethod.MethodType.BANK_TRANSFER
        tracker.logEvent(
            BUY_BUTTON_PRESSED,
            mutableMapOf(
                "Sum_Currency" to buySumCurrency,
                "Sum_Coin" to buySumCoin,
                "Currency" to buyCurrency,
                "Coin" to buyCoin,
                "Payment_Method" to methodPayment?.toType().orEmpty(),
                "Bank_Transfer" to isBankTransfer,
            ).apply {
                if (isBankTransfer) {
                    put("Type_Bank_Transfer", methodPayment?.paymentType.orEmpty())
                }
            }
        )
    }

    fun logBuyTotalShown() {
        tracker.logEvent(BUY_TOTAL_SHOWED)
    }

    fun logBuyMoonPayOpened() {
        tracker.logEvent(BUY_MOONPAY_WINDOW_OPENED)
    }

    private fun PaymentMethod.toType(): String {
        return when (this.method) {
            PaymentMethod.MethodType.CARD -> "Card"
            PaymentMethod.MethodType.BANK_TRANSFER -> "BankTransfer"
        }
    }

    enum class BuyResult(val title: String) {
        SUCCESS("Success"),
        ERROR("Error")
    }
}

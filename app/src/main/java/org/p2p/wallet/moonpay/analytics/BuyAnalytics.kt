package org.p2p.wallet.moonpay.analytics

import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_BUTTON_PRESSED
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_CHANGING_PROVIDER
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_CHOSEN_METHOD_PAYMENT
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_COIN_CHANGED
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_CONTINUING
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_CURRENCY_CHANGED
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_GOING_BACK
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_LIST_VIEWED
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_MOONPAY_WINDOW
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_PAYMENT_RESULT_SHOWN
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_PROVIDER_STEP_VIEWED
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_TOKEN_CHOSEN
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_TOTAL_SHOWED
import org.p2p.wallet.common.analytics.constants.EventNames.BUY_VIEWED
import org.p2p.wallet.moonpay.model.PaymentMethod
import java.math.BigDecimal

class BuyAnalytics(private val tracker: Analytics) {

    fun logBuyViewed() {
        tracker.logEvent(BUY_VIEWED)
    }

    fun logBuyListViewed() {
        tracker.logEvent(BUY_LIST_VIEWED)
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

    fun logBuyChangingProvider(buyProvider: String, buyCountry: String) {
        tracker.logEvent(
            BUY_CHANGING_PROVIDER,
            arrayOf(
                Pair("Buy_Provider", buyProvider),
                Pair("Buy_Country", buyCountry)
            )
        )
    }

    fun logBuyProviderStepViewed(stepName: String) {
        tracker.logEvent(
            BUY_PROVIDER_STEP_VIEWED,
            arrayOf(
                Pair("Buy_Provider_Step_Name", stepName)
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
            arrayOf(
                Pair("From_Currency", fromCurrency),
                Pair("To_Currency", toCurrency)
            )
        )
    }

    fun logBuyTokenChanged(fromToken: String, toToken: String) {
        tracker.logEvent(
            BUY_COIN_CHANGED,
            arrayOf(
                Pair("From_Coin", fromToken),
                Pair("To_Coin", toToken)
            )
        )
    }

    fun logBuyMethodPaymentChanged(methodPayment: PaymentMethod) {
        tracker.logEvent(
            BUY_CHOSEN_METHOD_PAYMENT,
            arrayOf(
                Pair("Type", methodPayment.toType())
            )
        )
    }

    fun logBuyButtonPressed(
        buySumCurrency: BigDecimal,
        buySumCoin: BigDecimal,
        buyCurrency: String,
        buyCoin: String,
        methodPayment: PaymentMethod
    ) {
        val isBankTransfer = methodPayment.method == PaymentMethod.MethodType.BANK_TRANSFER
        tracker.logEvent(
            BUY_BUTTON_PRESSED,
            arrayOf(
                Pair("Sum_Currency", buySumCurrency),
                Pair("Sum_Coin", buySumCoin),
                Pair("Currency", buyCurrency),
                Pair("Coin", buyCoin),
                Pair("Payment_Method", methodPayment.toType()),
                Pair("Bank_Transfer", isBankTransfer),
                Pair("Type_Bank_Transfer", methodPayment.paymentType),
            )
        )
    }

    fun logBuyTotalShown(isShown: Boolean) {
        tracker.logEvent(
            BUY_TOTAL_SHOWED,
            arrayOf(
                Pair("Shown", isShown)
            )
        )
    }

    fun logBuyMoonPayOpened() {
        tracker.logEvent(
            BUY_MOONPAY_WINDOW,
            arrayOf(
                Pair("Opened", true)
            )
        )
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

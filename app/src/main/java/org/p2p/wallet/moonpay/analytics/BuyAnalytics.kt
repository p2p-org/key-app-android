package org.p2p.wallet.moonpay.analytics

import org.p2p.wallet.common.analytics.TrackerContract
import java.math.BigDecimal

class BuyAnalytics(private val tracker: TrackerContract) {

    fun logBuyViewed() {
        tracker.logEvent("BuyViewed")
    }
    fun logBuyListViewed() {
        tracker.logEvent("Buy_List_Viewed")
    }

    fun logBuyTokenChosen(tokenName: String, lastScreenName: String) {
        tracker.logEvent(
            "Buy_Token_Chosen",
            arrayOf(
                Pair("Token_Name", tokenName),
                Pair("Last_Screen", lastScreenName)
            )
        )
    }

    fun logBuyGoingBack(buySum: BigDecimal, buyCurrency: String, buyUSD: BigDecimal) {
        tracker.logEvent(
            "Buy_Going_Back",
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
            "Buy_Continuing",
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
            "Buy_Changing_Provider",
            arrayOf(
                Pair("Buy_Provider", buyProvider),
                Pair("Buy_Country", buyCountry)
            )
        )
    }

    fun logBuyProviderStepViewed(stepName: String) {
        tracker.logEvent(
            "Buy_Provider_Step_Viewed",
            arrayOf(
                Pair("Buy_Provider_Step_Name", stepName)
            )
        )
    }

    fun logBuyPaymentResultShown(result: BuyResult) {
        tracker.logEvent(
            "Buy_Payment_Result_Shown",
            arrayOf(
                Pair("Buy_Result", result.title)
            )
        )
    }

    enum class BuyResult(val title: String) {
        SUCCESS("Success"),
        ERROR("Error")
    }
}
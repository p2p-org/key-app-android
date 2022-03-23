package org.p2p.wallet.moonpay.analytics

import org.p2p.wallet.common.analytics.Events.BUY_CHANGING_PROVIDER
import org.p2p.wallet.common.analytics.Events.BUY_CONTINUING
import org.p2p.wallet.common.analytics.Events.BUY_GOING_BACK
import org.p2p.wallet.common.analytics.Events.BUY_LIST_VIEWED
import org.p2p.wallet.common.analytics.Events.BUY_PAYMENT_RESULT_SHOWN
import org.p2p.wallet.common.analytics.Events.BUY_PROVIDER_STEP_VIEWED
import org.p2p.wallet.common.analytics.Events.BUY_TOKEN_CHOSEN
import org.p2p.wallet.common.analytics.Events.BUY_VIEWED
import org.p2p.wallet.common.analytics.TrackerContract
import java.math.BigDecimal

class BuyAnalytics(private val tracker: TrackerContract) {

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

    enum class BuyResult(val title: String) {
        SUCCESS("Success"),
        ERROR("Error")
    }
}

package org.p2p.wallet.sell.analytics

import org.p2p.wallet.common.analytics.Analytics

private const val SELL_CLICKED = "Sell_Clicked"
private const val SELL_CLICKED_SERVER_ERROR = "Sell_Clicked_Server_Error"
private const val SELL_CLICKED_SORRY_MIN_AMOUNT = "Sell_Clicked_Sorry_Min_Amount"
private const val SELL_SORRY_MIN_AMOUNT_SWAP = "Sell_Sorry_Min_Amount_Swap"
private const val SELL_FINISH_SEND = "Sell_Finish_Send"
private const val SELL_AMOUNT = "Sell_Amount"
private const val SELL_AMOUNT_NEXT = "Sell_Amount_Next"
private const val SELL_MOONPAY = "Sell_Moonpay"
private const val SELL_ONLY_SOL_NOTIFICATION = "Sell_Only_SOL_Notification"
private const val SELL_MOONPAY_OPEN_NOTIFICATION = "Sell_Moonpay_Open_Notification"

class SellAnalytics(
    private val tracker: Analytics
) {
    fun logCashOutClicked(source: AnalyticsCashOutSource) {
        tracker.logEvent(
            event = SELL_CLICKED,
            params = mapOf("Source" to source.value)
        )
    }

    fun logSellServerErrorOpened() {
        tracker.logEvent(SELL_CLICKED_SERVER_ERROR)
    }

    fun logSellErrorMinAmountOpened() {
        tracker.logEvent(SELL_CLICKED_SORRY_MIN_AMOUNT)
    }

    fun logSellErrorMinAmountSwapClicked() {
        tracker.logEvent(SELL_SORRY_MIN_AMOUNT_SWAP)
    }

    fun logSellLockedOpened() {
        tracker.logEvent(SELL_FINISH_SEND)
    }

    fun logSellTokenAmountFocused() {
        tracker.logEvent(SELL_AMOUNT)
    }

    fun logSellSubmitClicked() {
        tracker.logEvent(SELL_AMOUNT_NEXT)
    }

    fun logSellMoonpayOpened() {
        tracker.logEvent(SELL_MOONPAY)
    }

    fun logSellOnlySolWarningClosed() {
        tracker.logEvent(event = SELL_ONLY_SOL_NOTIFICATION)
    }

    fun logSellMoonpayInformationClosed() {
        tracker.logEvent(event = SELL_MOONPAY_OPEN_NOTIFICATION)
    }

    enum class AnalyticsCashOutSource(val value: String) {
        MAIN("Main"), ACTION_BUTTON("Action_Panel")
    }
}

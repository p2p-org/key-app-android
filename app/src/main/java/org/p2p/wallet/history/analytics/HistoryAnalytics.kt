package org.p2p.wallet.history.analytics

import org.p2p.core.analytics.Analytics
import org.p2p.core.utils.orZero
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.swap.analytics.SwapAnalytics

private const val HISTORY_OPENED = "History_Opened"
private const val HISTORY_SEND_CLICKED = "History_Send_Clicked"

private const val HISTORY_CLICK_BLOCK_SEND_VIA_LINK = "History_Click_Block_Send_Via_Link"
private const val HISTORY_SEND_CLICK_TRANSACTION = "History_Send_Click_Transaction"
private const val HISTORY_SEND_CLICK_COPY_TRANSACTION = "History_Send_Click_Copy_Transaction"
private const val HISTORY_SEND_CLICK_SHARE_TRANSACTION = "History_Send_Click_Share_Transaction"
private const val HISTORY_TOKEN_SCREEN_CLICK_TRANSACTION = "Token_Screen_Transaction"

class HistoryAnalytics(
    private val tracker: Analytics,
    private val swapAnalytics: SwapAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
) {

    fun onScreenOpened(isSendViaLinkBlockVisible: Boolean) {
        tracker.logEvent(
            event = HISTORY_OPENED,
            params = mapOf("Sent_Via_Link" to isSendViaLinkBlockVisible)
        )
    }

    fun logTokenTransactionClicked(transactionId: String) {
        tracker.logEvent(
            event = HISTORY_TOKEN_SCREEN_CLICK_TRANSACTION,
            params = mapOf("transaction_id" to transactionId)
        )
    }

    fun logUserSendLinksBlockClicked() {
        tracker.logEvent(HISTORY_CLICK_BLOCK_SEND_VIA_LINK)
    }

    fun logUserSendLinkClicked() {
        tracker.logEvent(HISTORY_SEND_CLICK_TRANSACTION)
    }

    fun logUserSendLinkCopyClicked() {
        tracker.logEvent(HISTORY_SEND_CLICK_COPY_TRANSACTION)
    }

    fun logUserSendLinkShareClicked() {
        tracker.logEvent(HISTORY_SEND_CLICK_SHARE_TRANSACTION)
    }

    fun logSwapTransactionClicked(transaction: RpcHistoryTransaction.Swap) {
        swapAnalytics.logSwapShowingDetails(
            swapStatus = SwapAnalytics.SwapStatus.SUCCESS,
            lastScreenName = analyticsInteractor.getPreviousScreenName(),
            tokenAName = transaction.tokenA.symbol,
            tokenBName = transaction.tokenB.symbol,
            swapSum = transaction.tokenAAmount.total,
            swapUSD = transaction.tokenBAmount.totalInUsd.orZero(),
            feesSource = SwapAnalytics.FeeSource.UNKNOWN
        )
    }

    fun logSellTransactionClicked(transaction: SellTransactionViewDetails) {
        tracker.logEvent(
            event = HISTORY_SEND_CLICKED,
            params = mapOf("Status" to transaction.status.toAnalyticsValue())
        )
    }

    private fun SellTransactionStatus.toAnalyticsValue(): String = when (this) {
        SellTransactionStatus.WAITING_FOR_DEPOSIT -> "waiting_for_deposit"
        SellTransactionStatus.PENDING -> "processing"
        SellTransactionStatus.FAILED -> "expired"
        SellTransactionStatus.COMPLETED -> "sent"
    }
}

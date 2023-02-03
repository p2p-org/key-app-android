package org.p2p.wallet.history.analytics

import org.p2p.core.utils.orZero
import org.p2p.wallet.common.analytics.Analytics
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.swap.analytics.SwapAnalytics

private const val HISTORY_SEND_CLICKED = "History_Send_Clicked"

class HistoryAnalytics(
    private val tracker: Analytics,
    private val sendAnalytics: NewSendAnalytics,
    private val receiveAnalytics: ReceiveAnalytics,
    private val swapAnalytics: SwapAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
) {

    fun logSwapTransactionClicked(transaction: HistoryTransaction.Swap) {
        swapAnalytics.logSwapShowingDetails(
            swapStatus = SwapAnalytics.SwapStatus.SUCCESS,
            lastScreenName = analyticsInteractor.getPreviousScreenName(),
            tokenAName = transaction.sourceSymbol,
            tokenBName = transaction.destinationSymbol,
            swapSum = transaction.amountA,
            swapUSD = transaction.amountSentInUsd.orZero(),
            feesSource = SwapAnalytics.FeeSource.UNKNOWN
        )
    }

    fun logTransferTransactionClicked(
        transaction: HistoryTransaction.Transfer,
        isRenBtcSessionActive: Boolean
    ) {
        if (transaction.isSend) {
            val sendNetwork = if (isRenBtcSessionActive) {
                NewSendAnalytics.AnalyticsSendNetwork.BITCOIN
            } else {
                NewSendAnalytics.AnalyticsSendNetwork.SOLANA
            }
            sendAnalytics.logSendShowingDetails(
                sendStatus = NewSendAnalytics.SendStatus.SUCCESS,
                lastScreenName = analyticsInteractor.getPreviousScreenName(),
                tokenName = transaction.tokenData.symbol,
                sendNetwork = sendNetwork,
                sendSum = transaction.total,
                sendUSD = transaction.totalInUsd.orZero()
            )
        } else {
            val receiveNetwork = if (isRenBtcSessionActive) {
                ReceiveAnalytics.ReceiveNetwork.BITCOIN
            } else {
                ReceiveAnalytics.ReceiveNetwork.SOLANA
            }
            receiveAnalytics.logReceiveShowingDetails(
                receiveSum = transaction.total,
                receiveUSD = transaction.totalInUsd.orZero(),
                tokenName = transaction.tokenData.symbol,
                receiveNetwork = receiveNetwork
            )
        }
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

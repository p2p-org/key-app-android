package org.p2p.wallet.history.analytics

import org.p2p.core.utils.orZero
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.swap.analytics.SwapAnalytics

class HistoryAnalytics(
    private val sendAnalytics: SendAnalytics,
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
                SendAnalytics.AnalyticsSendNetwork.BITCOIN
            } else {
                SendAnalytics.AnalyticsSendNetwork.SOLANA
            }
            sendAnalytics.logSendShowingDetails(
                sendStatus = SendAnalytics.SendStatus.SUCCESS,
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
}

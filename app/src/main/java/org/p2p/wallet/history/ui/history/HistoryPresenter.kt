package org.p2p.wallet.history.ui.history

import kotlinx.coroutines.launch
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.rpc.interactor.TokenInteractor
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.swap.analytics.SwapAnalytics
import timber.log.Timber
import java.math.BigDecimal
import kotlin.properties.Delegates

private const val PAGE_SIZE = 20

class HistoryPresenter(
    private val historyInteractor: HistoryInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val renBtcInteractor: RenBtcInteractor,
    private val receiveAnalytics: ReceiveAnalytics,
    private val swapAnalytics: SwapAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val sendAnalytics: SendAnalytics
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    private val publicKey = tokenKeyProvider.publicKey

    private var transactions by Delegates.observable<List<HistoryTransaction>>(emptyList()) { _, _, newValue ->
        view?.showHistory(items = newValue)
        view?.showPagingState(PagingState.Idle)
    }

    override fun loadHistory(isRefresh: Boolean) {
        launch {
            if (transactions.isEmpty()) {
                view?.showPagingState(PagingState.InitialLoading)
            } else {
                view?.showPagingState(PagingState.Loading)
            }
            val lastSignature = transactions.lastOrNull()?.signature
            runCatching {
                historyInteractor.getTransactionHistory2(
                    tokenPublicKey = publicKey,
                    forceNetwork = isRefresh,
                    limit = PAGE_SIZE,
                    lastSignature = lastSignature
                )
            }
                .onSuccess(::onSuccess)
                .onFailure(::onFailure)
        }
    }

    override fun onItemClicked(transaction: HistoryTransaction) {
        launch {
            when (transaction) {
                is HistoryTransaction.Swap -> {
                    swapAnalytics.logSwapShowingDetails(
                        swapStatus = SwapAnalytics.SwapStatus.SUCCESS,
                        lastScreenName = analyticsInteractor.getPreviousScreenName(),
                        tokenAName = transaction.sourceSymbol,
                        tokenBName = transaction.destinationSymbol,
                        swapSum = transaction.amountA,
                        swapUSD = transaction.amountSentInUsd ?: BigDecimal.ZERO,
                        feesSource = SwapAnalytics.FeeSource.UNKNOWN
                    )
                }
                is HistoryTransaction.Transfer -> {
                    val renBtcSession = renBtcInteractor.findActiveSession()
                    val isRenBtcSessionActive = renBtcSession != null && renBtcSession.isValid

                    if (transaction.isSend) {
                        val sendNetwork =
                            if (isRenBtcSessionActive) {
                                SendAnalytics.SendNetwork.BITCOIN
                            } else {
                                SendAnalytics.SendNetwork.SOLANA
                            }
                        sendAnalytics.logSendShowingDetails(
                            sendStatus = SendAnalytics.SendStatus.SUCCESS,
                            lastScreenName = analyticsInteractor.getPreviousScreenName(),
                            tokenName = transaction.tokenData.symbol,
                            sendNetwork = sendNetwork,
                            sendSum = transaction.total,
                            sendUSD = transaction.totalInUsd ?: BigDecimal.ZERO
                        )
                    } else {
                        val receiveNetwork =
                            if (isRenBtcSessionActive) {
                                ReceiveAnalytics.ReceiveNetwork.BITCOIN
                            } else {
                                ReceiveAnalytics.ReceiveNetwork.SOLANA
                            }
                        receiveAnalytics.logReceiveShowingDetails(
                            receiveSum = transaction.total,
                            receiveUSD = transaction.totalInUsd ?: BigDecimal.ZERO,
                            tokenName = transaction.tokenData.symbol,
                            receiveNetwork = receiveNetwork
                        )
                    }
                }
                else -> {
                    // TODO: Add support for other transaction types
                    // do nothing yet
                    return@launch
                }
            }

            view?.openTransactionDetailsScreen(transaction)
        }
    }

    private fun onSuccess(items: List<HistoryTransaction>) {
        transactions = transactions + items
    }

    private fun onFailure(e: Throwable) {
        Timber.e("Error getting transaction history")
        if (e is EmptyDataException) {
            transactions = transactions.toMutableList()
        } else {
            view?.showPagingState(PagingState.Error(e))
        }
    }
}

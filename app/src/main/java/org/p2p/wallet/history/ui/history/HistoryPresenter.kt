package org.p2p.wallet.history.ui.history

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.swap.analytics.SwapAnalytics
import timber.log.Timber
import java.math.BigDecimal

class HistoryPresenter(
    private val historyInteractor: HistoryInteractor,
    private val renBtcInteractor: RenBtcInteractor,
    private val receiveAnalytics: ReceiveAnalytics,
    private val swapAnalytics: SwapAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val environmentManager: NetworkEnvironmentManager,
    private val sendAnalytics: SendAnalytics
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    private var isPagingEnded = false
    private var refreshJob: Job? = null
    private var pagingJob: Job? = null

    private var lastTransactionSignature: String? = null
    private var transactions = mutableListOf<HistoryTransaction>()

    override fun attach(view: HistoryContract.View) {
        super.attach(view)
        environmentManager.addEnvironmentListener(this::class) {
            refreshHistory()
        }
    }

    override fun refreshHistory() {
        isPagingEnded = false
        lastTransactionSignature = null
        refreshJob?.cancel()
        transactions.clear()
        pagingJob?.cancel()

        refreshJob = launch {
            view?.showRefreshing(isRefreshing = true)
            fetchHistory(isRefresh = true)
            view?.showRefreshing(isRefreshing = false)
            view?.scrollToTop()
        }
    }

    override fun loadNextHistoryPage() {
        if (isPagingEnded) return

        if (pagingJob?.isActive == true) {
            return
        }
        pagingJob = launch {
            view?.showPagingState(PagingState.Loading)
            fetchHistory()
        }
    }

    override fun loadHistory() {
        if (transactions.isNotEmpty()) {
            view?.showHistory(transactions)
            return
        }
        launch {
            view?.showPagingState(PagingState.InitialLoading)
            fetchHistory()
        }
    }

    private suspend fun fetchHistory(isRefresh: Boolean = false) {
        try {
            val fetchedItems = historyInteractor.loadTransactions(isRefresh)
            transactions.addAll(fetchedItems)
            view?.showHistory(transactions)
            view?.showPagingState(PagingState.Idle)
        } catch (e: CancellationException) {
            Timber.w(e, "Cancelled history next page load")
        } catch (e: EmptyDataException) {
            if (transactions.isEmpty()) {
                view?.showHistory(emptyList())
                isPagingEnded = true
            }
            view?.showPagingState(PagingState.Idle)
        } catch (e: Throwable) {
            view?.showPagingState(PagingState.Error(e))
            Timber.e(e, "Error getting transaction history")
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
}

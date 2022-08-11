package org.p2p.wallet.history.ui.token

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.widget.ActionButtonsView.ActionButton
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.rpc.interactor.TokenInteractor
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.swap.analytics.SwapAnalytics
import timber.log.Timber
import java.math.BigDecimal

class TokenHistoryPresenter(
    private val token: Token.Active,
    private val historyInteractor: HistoryInteractor,
    private val receiveAnalytics: ReceiveAnalytics,
    private val swapAnalytics: SwapAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val sendAnalytics: SendAnalytics,
    private val renBtcInteractor: RenBtcInteractor,
    private val tokenInteractor: TokenInteractor
) : BasePresenter<TokenHistoryContract.View>(), TokenHistoryContract.Presenter {

    private val transactions = mutableListOf<HistoryTransaction>()
    private val actions = mutableListOf(
        ActionButton(R.string.home_receive, R.drawable.ic_receive_simple),
        ActionButton(R.string.home_send, R.drawable.ic_send_medium),
        ActionButton(R.string.main_swap, R.drawable.ic_swap_medium)
    )

    init {
        if (token.isSOL || token.isUSDC) {
            actions.add(0, ActionButton(R.string.home_buy, R.drawable.ic_plus))
        }
    }

    override fun attach(view: TokenHistoryContract.View) {
        super.attach(view)
        view.showActions(actions)
    }

    private var pagingJob: Job? = null
    private var refreshJob: Job? = null

    private var paginationEnded: Boolean = false

    override fun loadNextHistoryPage() {
        if (paginationEnded) return

        if (pagingJob?.isActive == true) {
            return
        }
        pagingJob = launch {
            view?.showPagingState(PagingState.Loading)
            fetchHistory()
        }
    }

    override fun retryLoad() {
        paginationEnded = false
        refreshJob?.cancel()

        refreshJob = launch {
            view?.showRefreshing(isRefreshing = true)
            fetchHistory(isRefresh = true)
            view?.showRefreshing(isRefreshing = false)
            view?.scrollToTop()
        }
    }

    override fun loadHistory() {
        if (transactions.isNotEmpty()) {
            view?.showHistory(transactions)
            return
        }
        launch {
            view?.showPagingState(PagingState.InitialLoading)
            fetchHistory(transactions.isEmpty())
        }
    }

    private suspend fun fetchHistory(isRefresh: Boolean = false) {
        try {
            if (isRefresh) {
                transactions.clear()
            }
            val fetchedItems = historyInteractor.loadTransactions(token.publicKey, isRefresh)
            transactions.addAll(fetchedItems)
            view?.showHistory(transactions)
            view?.showPagingState(PagingState.Idle)
        } catch (e: CancellationException) {
            Timber.w(e, "Cancelled history next page load")
        } catch (e: EmptyDataException) {
            if (transactions.isEmpty()) {
                view?.showHistory(emptyList())
                paginationEnded = true
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

    override fun closeAccount() {
        launch {
            try {
                tokenInteractor.closeTokenAccount(token.publicKey)
                view?.showErrorSnackBar(R.string.details_account_closed_successfully)
            } catch (e: Throwable) {
                Timber.e(e, "Error closing account: ${token.publicKey}")
                view?.showErrorMessage(e)
            }
        }
    }
}

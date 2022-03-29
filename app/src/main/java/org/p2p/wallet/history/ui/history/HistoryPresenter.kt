package org.p2p.wallet.history.ui.history

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

private const val PAGE_SIZE = 20

class HistoryPresenter(
    private val token: Token.Active,
    private val historyInteractor: HistoryInteractor,
    private val receiveAnalytics: ReceiveAnalytics,
    private val swapAnalytics: SwapAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val sendAnalytics: SendAnalytics,
    private val renBtcInteractor: RenBtcInteractor,
    private val tokenInteractor: TokenInteractor
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    private val transactions = mutableListOf<HistoryTransaction>()
    private val actions = mutableListOf(
        ActionButton(R.string.main_receive, R.drawable.ic_receive_simple),
        ActionButton(R.string.main_send, R.drawable.ic_send_medium),
        ActionButton(R.string.main_swap, R.drawable.ic_swap_medium)
    )

    init {
        if (token.isSOL || token.isUSDC) {
            actions.add(0, ActionButton(R.string.main_buy, R.drawable.ic_plus))
        }
    }

    override fun attach(view: HistoryContract.View) {
        super.attach(view)
        view.showActions(actions)
    }

    private var pagingJob: Job? = null
    private var refreshJob: Job? = null

    private var paginationEnded: Boolean = false

    override fun loadHistory() {
        if (transactions.isNotEmpty()) return

        paginationEnded = false

        launch {
            view?.showPagingState(PagingState.Loading)

            kotlin.runCatching {
                historyInteractor.getAllHistoryTransactions(
                    tokenPublicKey = token.publicKey,
                    before = null,
                    limit = PAGE_SIZE,
                    forceRefresh = false
                )
            }
                .onSuccess(::handleLoadHistorySuccess)
                .onFailure(::handleLoadHistoryFailure)
        }
    }

    private fun handleLoadHistorySuccess(historyTransactions: List<HistoryTransaction>) {
        if (historyTransactions.isEmpty()) {
            paginationEnded = true
        } else {
            transactions.addAll(historyTransactions)
            view?.showHistory(transactions)
        }

        view?.showPagingState(PagingState.Idle)
    }

    private fun handleLoadHistoryFailure(e: Throwable) {
        Timber.e(e, "Error getting transaction history")

        if (e is EmptyDataException) {
            view?.showPagingState(PagingState.Idle)
            if (transactions.isEmpty()) view?.showHistory(emptyList())
        } else {
            view?.showPagingState(PagingState.Error(e))
        }
    }

    override fun refresh() {
        paginationEnded = false

        refreshJob?.cancel()
        refreshJob = launch {
            try {
                view?.showRefreshing(true)
                transactions.clear()
                val history = historyInteractor.getAllHistoryTransactions(token.publicKey, null, PAGE_SIZE, true)
                if (history.isEmpty()) {
                    paginationEnded = true
                } else {
                    transactions.addAll(history)
                    view?.showHistory(transactions)
                }
                view?.showPagingState(PagingState.Idle)
            } catch (e: CancellationException) {
                Timber.w(e, "Cancelled history refresh")
            } catch (e: Throwable) {
                Timber.e(e, "Error refreshing transaction history")
                if (e is EmptyDataException) {
                    view?.showPagingState(PagingState.Idle)
                    if (transactions.isEmpty()) view?.showHistory(emptyList())
                } else {
                    view?.showPagingState(PagingState.Error(e))
                }
            } finally {
                view?.showRefreshing(false)
            }
        }
    }

    override fun fetchNextPage() {
        if (paginationEnded) return

        pagingJob?.cancel()
        pagingJob = launch {
            try {
                view?.showPagingState(PagingState.Loading)

                val lastSignature = transactions.lastOrNull()?.signature
                val newHistoryPage = historyInteractor.getAllHistoryTransactions(
                    tokenPublicKey = token.publicKey,
                    before = lastSignature,
                    limit = PAGE_SIZE,
                    forceRefresh = false
                )
                if (newHistoryPage.isEmpty()) {
                    paginationEnded = true
                } else {
                    transactions.addAll(newHistoryPage)
                    view?.showHistory(transactions)
                }

                view?.showPagingState(PagingState.Idle)
            } catch (e: CancellationException) {
                Timber.w(e, "Cancelled history next page load")
            } catch (e: Throwable) {
                Timber.e(e, "Error getting transaction history")
                if (e is EmptyDataException) {
                    paginationEnded = true
                    view?.showPagingState(PagingState.Idle)
                } else {
                    view?.showPagingState(PagingState.Error(e))
                }
            }
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

package org.p2p.wallet.history.ui.token

import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.ui.history.HistorySellTransactionMapper
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.rpc.interactor.TokenInteractor
import org.p2p.wallet.send.analytics.SendAnalytics
import org.p2p.wallet.swap.analytics.SwapAnalytics
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TokenHistoryPresenter(
    private val token: Token.Active,
    private val historyInteractor: HistoryInteractor,
    private val receiveAnalytics: ReceiveAnalytics,
    private val swapAnalytics: SwapAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val sendAnalytics: SendAnalytics,
    private val renBtcInteractor: RenBtcInteractor,
    private val tokenInteractor: TokenInteractor,
    private val sellTransactionsMapper: HistorySellTransactionMapper,
) : BasePresenter<TokenHistoryContract.View>(), TokenHistoryContract.Presenter {

    private val transactions = mutableListOf<HistoryTransaction>()
    private val moonpayTransactions = mutableListOf<SellTransaction>()

    private var pagingJob: Job? = null

    private var refreshJob: Job? = null
    private var paginationEnded: Boolean = false

    override fun attach(view: TokenHistoryContract.View) {
        super.attach(view)
        initialize()
    }

    private fun initialize() {
        val actionButtons = mutableListOf(
            ActionButton.RECEIVE_BUTTON,
            ActionButton.SEND_BUTTON,
            ActionButton.SWAP_BUTTON
        )

        if (token.isSOL || token.isUSDC) {
            actionButtons.add(0, ActionButton.BUY_BUTTON)
        }

        view?.showActionButtons(actionButtons)
    }

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
        if (transactions.isNotEmpty() || moonpayTransactions.isNotEmpty()) {
            view?.showHistory(transactions, moonpayTransactions)
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
                moonpayTransactions.clear()
            }
            val fetchedItems = historyInteractor.loadTransactions(token.publicKey, isRefresh)
            if (token.isSOL) {
                val sellTransactions = historyInteractor.loadSellTransactions()
                    .filter { transaction -> !transaction.isCancelled() }
                moonpayTransactions.clear()
                moonpayTransactions.addAll(sellTransactionsMapper.map(sellTransactions))
            }
            transactions.addAll(fetchedItems)
            view?.showHistory(transactions, moonpayTransactions)
            view?.showPagingState(PagingState.Idle)
        } catch (e: CancellationException) {
            Timber.i(e, "Cancelled history next page load")
        } catch (e: EmptyDataException) {
            if (transactions.isEmpty()) {
                view?.showHistory(emptyList(), moonpayTransactions)
                paginationEnded = true
            }
            view?.showPagingState(PagingState.Idle)
        } catch (e: Throwable) {
            view?.showPagingState(PagingState.Error(e))
            Timber.e(e, "Error getting transaction history for token")
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

            view?.showDetailsScreen(transaction)
        }
    }

    override fun closeAccount() {
        launch {
            try {
                tokenInteractor.closeTokenAccount(token.publicKey)
                view?.showUiKitSnackBar(messageResId = R.string.details_account_closed_successfully)
            } catch (e: Throwable) {
                Timber.e(e, "Error closing account: ${token.publicKey}")
                view?.showErrorMessage(e)
            }
        }
    }
}

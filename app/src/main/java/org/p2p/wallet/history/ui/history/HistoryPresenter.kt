package org.p2p.wallet.history.ui.history

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.history.interactor.HistoryFetchListResult
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.sell.HiddenSellTransactionsStorageContract
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber

class HistoryPresenter(
    private val userInteractor: UserInteractor,
    private val historyInteractor: HistoryInteractor,
    private val hiddenSellTransactionsStorage: HiddenSellTransactionsStorageContract,
    private val historyAnalytics: HistoryAnalytics,
    private val renBtcInteractor: RenBtcInteractor,
    private val environmentManager: NetworkEnvironmentManager,
    private val sellTransactionsMapper: HistorySellTransactionMapper,
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    private object HistoryFetchFailure : Throwable(message = "Both transactions were not fetch due to errors")

    private var isPagingEnded = false
    private var refreshJob: Job? = null
    private var pagingJob: Job? = null

    private var lastTransactionSignature: String? = null
    private var blockChainTransactionsList = HistoryFetchListResult<HistoryTransaction>()
    private var moonpayTransactionsList = HistoryFetchListResult<SellTransaction>()

    override fun attach(view: HistoryContract.View) {
        super.attach(view)
        environmentManager.addEnvironmentListener(this::class) { refreshHistory() }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        loadHistory()
    }

    override fun refreshHistory() {
        isPagingEnded = false
        lastTransactionSignature = null
        refreshJob?.cancel()
        blockChainTransactionsList.clearContent()
        moonpayTransactionsList.clearContent()
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
        if (blockChainTransactionsList.hasFetchedItems() || moonpayTransactionsList.hasFetchedItems()) {
            view?.showHistory(blockChainTransactionsList.content, moonpayTransactionsList.content)
            return
        }
        launch {
            view?.showPagingState(PagingState.InitialLoading)
            fetchHistory(isRefresh = true)
        }
    }

    override fun onBuyClicked() {
        launch {
            val tokensForBuy = userInteractor.getTokensForBuy()
            if (tokensForBuy.isEmpty()) return@launch
            view?.showBuyScreen(tokensForBuy.first())
        }
    }

    private suspend fun fetchHistory(isRefresh: Boolean = false) {
        try {
            val deferredSellTransactions = async { fetchSellTransactions() }
            val deferredBlockChainTransactions = async { fetchBlockChainTransactions(isRefresh) }
            moonpayTransactionsList = deferredSellTransactions.await()
            blockChainTransactionsList += deferredBlockChainTransactions.await()
            if (moonpayTransactionsList.isFailed && blockChainTransactionsList.isFailed) {
                view?.showPagingState(PagingState.Error(HistoryFetchFailure))
                Timber.e(HistoryFetchFailure, "Error getting transaction whole history")
            } else {
                view?.showHistory(
                    blockChainTransactions = blockChainTransactionsList.content,
                    sellTransactions = moonpayTransactionsList.content
                )
                view?.showPagingState(PagingState.Idle)
            }
        } catch (e: CancellationException) {
            Timber.w(e, "Cancelled history next page load")
        }
    }

    private suspend fun fetchBlockChainTransactions(isRefresh: Boolean): HistoryFetchListResult<HistoryTransaction> =
        try {
            historyInteractor.loadTransactions(isRefresh)
                .toMutableList()
                .let(::HistoryFetchListResult)
        } catch (error: Throwable) {
            Timber.e(error, "Error while loading blockchain transactions on history")
            HistoryFetchListResult(isFailed = true)
        }

    private suspend fun fetchSellTransactions(): HistoryFetchListResult<SellTransaction> =
        try {
            sellTransactionsMapper.map(historyInteractor.getSellTransactions())
                .toMutableList()
                .let(::HistoryFetchListResult)
                .withContentFilter {
                    !it.isCancelled() &&
                        !hiddenSellTransactionsStorage.isTransactionHidden(it.transactionId)
                }
        } catch (error: Throwable) {
            Timber.e(error, "Error while loading Moonpay sell transactions on history")
            HistoryFetchListResult(isFailed = true)
        }

    override fun onItemClicked(transaction: HistoryTransaction) {
        logTransactionClicked(transaction)
        view?.openTransactionDetailsScreen(transaction)
    }

    private fun logTransactionClicked(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap -> {
                historyAnalytics.logSwapTransactionClicked(transaction)
            }
            is HistoryTransaction.Transfer -> {
                launch {
                    historyAnalytics.logTransferTransactionClicked(
                        transaction = transaction,
                        isRenBtcSessionActive = renBtcInteractor.isUserHasActiveSession()
                    )
                }
            }
            else -> Unit
        }
    }

    override fun onSellTransactionClicked(sellTransaction: SellTransactionViewDetails) {
        historyAnalytics.logSellTransactionClicked(sellTransaction)
        view?.openSellTransactionDetails(sellTransaction)
    }
}

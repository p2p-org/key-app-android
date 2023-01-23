package org.p2p.wallet.history.ui.history

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.sell.HiddenSellTransactionsStorageContract
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import timber.log.Timber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HistoryPresenter(
    private val historyInteractor: HistoryInteractor,
    private val hiddenSellTransactionsStorage: HiddenSellTransactionsStorageContract,
    private val historyAnalytics: HistoryAnalytics,
    private val renBtcInteractor: RenBtcInteractor,
    private val environmentManager: NetworkEnvironmentManager,
    private val sellTransactionsMapper: HistorySellTransactionMapper,
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    private class FetchListResult<T>(
        content: MutableList<T> = mutableListOf(),
        val isFailed: Boolean = false,
    ) {
        private val innerContent = content

        private var contentFilter: ((T) -> Boolean)? = null

        val content: List<T>
            get() = contentFilter?.let(innerContent::filter) ?: innerContent

        fun withContentFilter(filter: (T) -> Boolean): FetchListResult<T> = apply {
            contentFilter = filter
        }

        fun hasFetchedItems(): Boolean = content.isNotEmpty()

        fun clearContent() {
            innerContent.clear()
        }
    }

    private object HistoryFetchFailure : Throwable(message = "Both transactions were not fetch due to errors")

    private var isPagingEnded = false
    private var refreshJob: Job? = null
    private var pagingJob: Job? = null

    private var lastTransactionSignature: String? = null
    private var blockChainTransactionsList = FetchListResult<HistoryTransaction>()
    private var moonpayTransactionsList = FetchListResult<SellTransaction>()

    override fun attach(view: HistoryContract.View) {
        super.attach(view)
        environmentManager.addEnvironmentListener(this::class) { refreshHistory() }
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
            fetchHistory()
        }
    }

    override fun updateSellTransactions() {
        view?.showHistory(
            blockChainTransactions = blockChainTransactionsList.content,
            sellTransactions = moonpayTransactionsList.content
        )
    }

    private suspend fun fetchHistory(isRefresh: Boolean = false) {
        try {
            val deferredSellTransactions = async { fetchSellTransactions() }
            val deferredBlockChainTransactions = async { fetchBlockChainTransactions(isRefresh) }
            moonpayTransactionsList = deferredSellTransactions.await()
            blockChainTransactionsList = deferredBlockChainTransactions.await()
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

    private suspend fun fetchBlockChainTransactions(isRefresh: Boolean): FetchListResult<HistoryTransaction> = try {
        historyInteractor.loadTransactions(isRefresh)
            .toMutableList()
            .let(::FetchListResult)
    } catch (error: Throwable) {
        Timber.e(error, "Error while loading blockchain transactions on history")
        FetchListResult(isFailed = true)
    }

    private suspend fun fetchSellTransactions(): FetchListResult<SellTransaction> = try {
        sellTransactionsMapper.map(historyInteractor.getSellTransactions())
            .toMutableList()
            .let(::FetchListResult)
            .withContentFilter {
                !it.isCancelled() &&
                    !hiddenSellTransactionsStorage.isTransactionHidden(it.transactionId)
            }
    } catch (error: Throwable) {
        Timber.e(error, "Error while loading Moonpay sell transactions on history")
        FetchListResult(isFailed = true)
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

package org.p2p.wallet.history.ui.historylist

import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.merge
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.interactor.HistoryFetchListResult
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.ui.history.HistorySellTransactionMapper
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.sell.HiddenSellTransactionsStorageContract
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.sell.interactor.HistoryItemMapper

class HistoryListViewPresenter(
    private val token: Token.Active?,
    private val historyInteractor: HistoryInteractor,
    private val hiddenSellTransactionsStorage: HiddenSellTransactionsStorageContract,
    private val environmentManager: NetworkEnvironmentManager,
    private val sellTransactionsMapper: HistorySellTransactionMapper,
    private val historyItemMapper: HistoryItemMapper,
) : BasePresenter<HistoryListViewContract.View>(), HistoryListViewContract.Presenter {

    private object HistoryFetchFailure : Throwable(message = "Both transactions were not fetch due to errors")

    private var isPagingEnded = false
    private var refreshJob: Job? = null
    private var pagingJob: Job? = null

    private var lastTransactionSignature: String? = null
    private var blockChainTransactionsList = HistoryFetchListResult<HistoryTransaction>()
    private var moonpayTransactionsList = HistoryFetchListResult<SellTransaction>()

    override fun attach(view: HistoryListViewContract.View) {
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
            val sellTransactions = historyItemMapper.fromDomainSell(moonpayTransactionsList.content)
            val blockchainTransactions = historyItemMapper.fromDomainBlockchain(blockChainTransactionsList.content)
            view?.showHistory(
                sellTransactions.merge( // goes first
                    blockchainTransactions
                )
            )
            return
        }
        launch {
            view?.showPagingState(PagingState.InitialLoading)
            fetchHistory(isRefresh = true)
        }
    }

    private suspend fun fetchHistory(isRefresh: Boolean = false) {
        try {
            val deferredSellTransactions = async { fetchSellTransactions() }
            val deferredBlockChainTransactions = async { fetchBlockChainTransactions(isRefresh) }
            if (token == null || token.isSOL) {
                moonpayTransactionsList = deferredSellTransactions.await()
            }
            blockChainTransactionsList += deferredBlockChainTransactions.await()
            if (moonpayTransactionsList.isFailed && blockChainTransactionsList.isFailed) {
                view?.showPagingState(PagingState.Error(HistoryFetchFailure))
                val errorMessage = if (token == null) {
                    "for whole history"
                } else {
                    "for token: $token"
                }
                Timber.e(HistoryFetchFailure, "Error getting transaction $errorMessage")
            } else {
                view?.showHistory(
                    historyItemMapper.fromDomainSell(moonpayTransactionsList.content).merge( // goes first
                        historyItemMapper.fromDomainBlockchain(blockChainTransactionsList.content)
                    )
                )
                view?.showPagingState(PagingState.Idle)
            }
        } catch (e: CancellationException) {
            Timber.i(e, "Cancelled history next page load")
        }
    }

    private suspend fun fetchBlockChainTransactions(isRefresh: Boolean): HistoryFetchListResult<HistoryTransaction> =
        try {
            val transactions = if (token == null) {
                historyInteractor.loadTransactions(isRefresh)
            } else {
                historyInteractor.loadTransactions(token.publicKey, isRefresh)
            }
            transactions.toMutableList()
                .let(::HistoryFetchListResult)
        } catch (error: Throwable) {
            Timber.e(error, "Error while loading blockchain transactions on history")
            HistoryFetchListResult(isFailed = true)
        }

    private suspend fun fetchSellTransactions(): HistoryFetchListResult<SellTransaction> =
        try {
            historyInteractor.getSellTransactions()?.let { sellTransactions ->
                sellTransactionsMapper.map(sellTransactions)
                    .toMutableList()
                    .let(::HistoryFetchListResult)
                    .withContentFilter {
                        !it.isCancelled() &&
                            !hiddenSellTransactionsStorage.isTransactionHidden(it.transactionId)
                    }
            } ?: HistoryFetchListResult(isFailed = true)
        } catch (error: Throwable) {
            Timber.e(error, "Error while loading Moonpay sell transactions on history")
            HistoryFetchListResult(isFailed = true)
        }
}

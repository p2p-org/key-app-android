package org.p2p.wallet.history.ui.historylist

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.sell.interactor.HistoryItemMapper

private const val PAGE_SIZE = 20

class HistoryListViewPresenter(
    private val historyInteractor: HistoryInteractor,
    private val environmentManager: NetworkEnvironmentManager,
    private val historyItemMapper: HistoryItemMapper
) : BasePresenter<HistoryListViewContract.View>(), HistoryListViewContract.Presenter {

    override fun attach(view: HistoryListViewContract.View) {
        super.attach(view)
        environmentManager.addEnvironmentListener(this::class) { refreshHistory() }
        attachToHistoryFlow()
    }

    override fun loadNextHistoryPage(mintAddress: String?) {
        launch {
            try {
                view?.showPagingState(PagingState.Loading)
                val result = historyInteractor.loadNextPage(PAGE_SIZE, mintAddress)
                val newHistoryTransactions = handlePagingResult(result)
                historyItemMapper.toAdapterItem(newHistoryTransactions)
                view?.showPagingState(PagingState.Idle)
            } catch (e: Throwable) {
                Timber.e("Error on loading next history page: $e")
                view?.showPagingState(PagingState.Error(e))
            }
        }
    }

    override fun loadHistory(mintAddress: String?) {
        launch {
            try {
                view?.showPagingState(PagingState.InitialLoading)
                Timber.tag("_____start").d("LoadHistory")
                val result = historyInteractor.loadHistory(PAGE_SIZE, mintAddress)
                val newHistoryTransactions = handlePagingResult(result)
                historyItemMapper.toAdapterItem(newHistoryTransactions)
            } catch (e: Throwable) {
                Timber.e(e, "Error on loading history: $e")
                view?.showPagingState(PagingState.Error(e))
            }
        }
    }

    override fun refreshHistory(mintAddress: String?) {
        launch {
            try {
                view?.showRefreshing(isRefreshing = true)
                val result = historyInteractor.loadHistory(PAGE_SIZE, mintAddress)
                val newHistoryTransactions = handlePagingResult(result)
                historyItemMapper.toAdapterItem(newHistoryTransactions)
            } catch (e: Throwable) {
                Timber.e(e, "Error on loading history: $e")
                view?.showPagingState(PagingState.Error(e))
            } finally {
                view?.showRefreshing(isRefreshing = false)
            }
        }
    }

    override fun onItemClicked(historyItem: HistoryItem) {
        launch {
            when (historyItem) {
                is HistoryItem.TransactionItem -> {
                    view?.onTransactionClicked(historyItem.transactionId)
                }
                is HistoryItem.MoonpayTransactionItem -> {
                    view?.onSellTransactionClicked(historyItem.transactionId)
                }
                else -> {
                    val errorMessage = "Unsupported Transaction click! $historyItem"
                    Timber.e(UnsupportedOperationException(errorMessage))
                }
            }
        }
    }

    private fun attachToHistoryFlow() {
        launch {
            historyItemMapper.getHistoryAdapterItemFlow()
                .distinctUntilChanged()
                .collectLatest { items ->
                    view?.showHistory(items)
                    view?.showPagingState(PagingState.Idle)
                }
        }
    }

    private fun handlePagingResult(result: HistoryPagingResult): List<HistoryTransaction> {
        return when (result) {
            is HistoryPagingResult.Error -> error(result.cause)
            is HistoryPagingResult.Success -> result.data
        }
    }
}

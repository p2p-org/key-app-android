package org.p2p.wallet.history.ui.historylist

import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
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
    private val historyItemMapper: HistoryItemMapper,
    private val token: Token.Active?
) : BasePresenter<HistoryListViewContract.View>(), HistoryListViewContract.Presenter {

    override fun attach(view: HistoryListViewContract.View) {
        super.attach(view)
        environmentManager.addEnvironmentListener(this::class) { refreshHistory() }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        loadHistory()
    }

    override fun loadNextHistoryPage() {
        launch {
            try {
                view?.showPagingState(PagingState.Loading)
                val result = historyInteractor.loadNextPage(PAGE_SIZE, token?.mintAddress)
                val newHistoryTransactions = handlePagingResult(result)
                val adapterItems = historyItemMapper.toAdapterItem(newHistoryTransactions)
                view?.showHistory(adapterItems)
                view?.showPagingState(PagingState.Idle)
            } catch (e: Throwable) {
                Timber.e("Error on loading next history page: $e")
                view?.showPagingState(PagingState.Error(e))
            }
        }
    }

    override fun loadHistory() {
        launch {
            try {
                view?.showPagingState(PagingState.InitialLoading)
                val result = historyInteractor.loadHistory(PAGE_SIZE, token?.mintAddress)
                val newHistoryTransactions = handlePagingResult(result)
                val adapterItems = historyItemMapper.toAdapterItem(newHistoryTransactions)
                view?.showHistory(adapterItems)
                view?.showPagingState(PagingState.Idle)
            } catch (e: Throwable) {
                Timber.e(e, "Error on loading history: $e")
                view?.showPagingState(PagingState.Error(e))
            }
        }
    }

    override fun refreshHistory() {
        launch {
            try {
                view?.showRefreshing(isRefreshing = true)
                val result = historyInteractor.loadHistory(PAGE_SIZE, token?.mintAddress)
                val newHistoryTransactions = handlePagingResult(result)
                val adapterItems = historyItemMapper.toAdapterItem(newHistoryTransactions)
                view?.showHistory(adapterItems)
                view?.showPagingState(PagingState.Idle)
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

    private fun handlePagingResult(result: HistoryPagingResult): List<HistoryTransaction> {
        return when (result) {
            is HistoryPagingResult.Error -> error(result.cause)
            is HistoryPagingResult.Success -> result.data
        }
    }
}

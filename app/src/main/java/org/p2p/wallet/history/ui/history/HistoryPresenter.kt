package org.p2p.wallet.history.ui.history

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.PagingState
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.network.EmptyDataException
import timber.log.Timber

class HistoryPresenter(
    private val historyInteractor: HistoryInteractor
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    companion object {
        private const val PAGE_SIZE = 20
    }

    private val transactions = mutableListOf<HistoryTransaction>()

    private var pagingJob: Job? = null

    private var paginationEnded: Boolean = false

    override fun loadHistory(publicKey: String, tokenSymbol: String) {
        if (paginationEnded) return

        pagingJob?.cancel()
        pagingJob = launch {
            try {
                val state = if (transactions.isEmpty()) PagingState.InitialLoading else PagingState.Loading
                view?.showPagingState(state)

                val lastSignature = transactions.lastOrNull()?.signature
                val history = historyInteractor.getHistory(publicKey, lastSignature, PAGE_SIZE)
                if (history.isEmpty()) {
                    paginationEnded = true
                } else {
                    transactions.addAll(history)
                    view?.showHistory(transactions)
                }

                view?.showPagingState(PagingState.Idle)
            } catch (e: CancellationException) {
                Timber.w(e, "Cancelled history load")
            } catch (e: Throwable) {
                Timber.e(e, "Error getting transaction history")
                if (e is EmptyDataException) {
                    view?.showPagingState(PagingState.Idle)
                    if (transactions.isEmpty()) view?.showHistory(emptyList())
                } else {
                    view?.showPagingState(PagingState.Error(e))
                }
            }
        }
    }

    override fun refreshHistory() {
        paginationEnded = false
    }
}
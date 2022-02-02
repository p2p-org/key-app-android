package org.p2p.wallet.history.ui.info

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.main.model.Token
import timber.log.Timber

class TokenInfoPresenter(
    private val token: Token.Active,
    private val historyInteractor: HistoryInteractor
) : BasePresenter<TokenInfoContract.View>(), TokenInfoContract.Presenter {

    companion object {
        private const val DESTINATION_TOKEN = "USD"
        private const val PAGE_SIZE = 20
    }

    private val transactions = mutableListOf<HistoryTransaction>()

    private var pagingJob: Job? = null
    private var refreshJob: Job? = null

    private var paginationEnded: Boolean = false

    override fun loadHistory() {
        if (transactions.isNotEmpty()) return

        paginationEnded = false

        launch {
            try {
                view?.showPagingState(PagingState.InitialLoading)

                val history = historyInteractor.getHistory(token.publicKey, null, PAGE_SIZE)
                if (history.isEmpty()) {
                    paginationEnded = true
                } else {
                    transactions.addAll(history)
                    view?.showHistory(transactions)
                }

                view?.showPagingState(PagingState.Idle)
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

    override fun refresh() {
        paginationEnded = false

        refreshJob?.cancel()
        refreshJob = launch {
            try {
                view?.showRefreshing(true)

                val history = historyInteractor.getHistory(token.publicKey, null, PAGE_SIZE)
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
                val history = historyInteractor.getHistory(token.publicKey, lastSignature, PAGE_SIZE)
                if (history.isEmpty()) {
                    paginationEnded = true
                } else {
                    transactions.addAll(history)
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
}
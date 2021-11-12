package org.p2p.wallet.history.ui.main

import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.PagingState
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.network.EmptyDataException
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber

class HistoryPresenter(
    private val historyInteractor: HistoryInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    companion object {
        private const val DESTINATION_TOKEN = "USD"
        private const val PAGE_SIZE = 20
    }

    private val transactions = mutableListOf<HistoryTransaction>()

    private var pagingJob: Job? = null

    private var paginationEnded: Boolean = false

    override fun loadSolAddress() {
        launch {
            val sol = userInteractor.findAccountAddress(Token.WRAPPED_SOL_MINT) ?: return@launch
            view?.showSolAddress(sol)
        }
    }

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
                    view?.showHistory(history)
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

    override fun loadDailyChartData(tokenSymbol: String, days: Int) {
        launch {
            try {
                view?.showLoading(true)
                val data = historyInteractor.getDailyPriceHistory(tokenSymbol, DESTINATION_TOKEN, days)
                val entries = data.mapIndexed { index, price -> Entry(index.toFloat(), price.close.toFloat()) }
                view?.showChartData(entries)
            } catch (e: CancellationException) {
                Timber.w(e, "Cancelled daily chart data loading")
            } catch (e: Throwable) {
                view?.showError(R.string.error_fetching_data_about_token, tokenSymbol)
                Timber.e(e, "Error loading token price history")
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun loadHourlyChartData(tokenSymbol: String, hours: Int) {
        launch {
            try {
                view?.showLoading(true)
                val data = historyInteractor.getHourlyPriceHistory(tokenSymbol, DESTINATION_TOKEN, hours)
                val entries = data.mapIndexed { index, price -> Entry(index.toFloat(), price.close.toFloat()) }
                view?.showChartData(entries)
            } catch (e: CancellationException) {
                Timber.w(e, "Cancelled hourly chart data loading")
            } catch (e: Throwable) {
                view?.showError(R.string.error_fetching_data_about_token, tokenSymbol)
                Timber.e(e, "Error loading token price history")
            } finally {
                view?.showLoading(false)
            }
        }
    }
}
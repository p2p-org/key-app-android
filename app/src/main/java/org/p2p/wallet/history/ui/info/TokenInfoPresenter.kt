package org.p2p.wallet.history.ui.info

import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.interactor.HistoryInteractor
import timber.log.Timber

class TokenInfoPresenter(
    private val historyInteractor: HistoryInteractor
) : BasePresenter<TokenInfoContract.View>(), TokenInfoContract.Presenter {

    companion object {
        private const val DESTINATION_TOKEN = "USD"
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
package com.p2p.wallet.token.ui

import com.github.mikephil.charting.data.Entry
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.interactor.MainInteractor
import com.p2p.wallet.token.interactor.TokenInteractor
import kotlinx.coroutines.launch
import timber.log.Timber

class TokenDetailsPresenter(
    private val tokenInteractor: TokenInteractor,
    private val mainInteractor: MainInteractor
) : BasePresenter<TokenDetailsContract.View>(), TokenDetailsContract.Presenter {

    companion object {
        private const val DESTINATION_TOKEN = "USD"
        private const val HISTORY_LIMIT = 10
    }

    override fun loadHistory(publicKey: String, tokenSymbol: String) {
        launch {
            try {
                view?.showLoading(true)
                val history = mainInteractor.getHistory(publicKey, tokenSymbol, HISTORY_LIMIT)
                view?.showHistory(history)
            } catch (e: Throwable) {
                Timber.e(e, "Error getting transaction history")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun loadDailyChartData(tokenSymbol: String, days: Int) {
        launch {
            try {
                val data = tokenInteractor.getDailyPriceHistory(tokenSymbol, DESTINATION_TOKEN, days)
                val entries = data.mapIndexed { index, price -> Entry(index.toFloat(), price.close.toFloat()) }
                view?.showChartData(entries)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading token price history")
            }
        }
    }

    override fun loadHourlyChartData(tokenSymbol: String, hours: Int) {
        launch {
            try {
                val data = tokenInteractor.getHourlyPriceHistory(tokenSymbol, DESTINATION_TOKEN, hours)
                val entries = data.mapIndexed { index, price -> Entry(index.toFloat(), price.close.toFloat()) }
                view?.showChartData(entries)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading token price history")
            }
        }
    }
}
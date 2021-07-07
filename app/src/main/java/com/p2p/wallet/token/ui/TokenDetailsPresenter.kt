package com.p2p.wallet.token.ui

import com.github.mikephil.charting.data.Entry
import com.p2p.wallet.R
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.main.interactor.MainInteractor
import com.p2p.wallet.token.interactor.TokenInteractor
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.properties.Delegates

class TokenDetailsPresenter(
    private val tokenInteractor: TokenInteractor,
    private val mainInteractor: MainInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<TokenDetailsContract.View>(), TokenDetailsContract.Presenter {

    companion object {
        private const val DESTINATION_TOKEN = "USD"
        private const val PAGE_SIZE = 20
    }

    private var transactions: List<Transaction> by Delegates.observable(emptyList()) { _, _, newValue ->
        view?.showHistory(newValue)
    }

    override fun loadSolAddress() {
        launch {
            val sol = userInteractor.findAccountAddress(Token.SOL_MINT) ?: return@launch
            view?.showSolAddress(sol)
        }
    }

    override fun loadHistory(publicKey: String, totalItemsCount: Int?, tokenSymbol: String) {
        launch {
            try {
                view?.showLoading(true)
                val lastSignature = totalItemsCount?.let { transactions[it - 1].signature }
                val history = mainInteractor.getHistory(publicKey, lastSignature, PAGE_SIZE)
                transactions = history
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
                view?.showError(R.string.error_fetching_data_about_token, tokenSymbol)
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
                view?.showError(R.string.error_fetching_data_about_token, tokenSymbol)
                Timber.e(e, "Error loading token price history")
            }
        }
    }
}
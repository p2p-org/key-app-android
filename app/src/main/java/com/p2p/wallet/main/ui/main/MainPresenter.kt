package com.p2p.wallet.main.ui.main

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode

class MainPresenter(
    private val userInteractor: UserInteractor
) : BasePresenter<MainContract.View>(), MainContract.Presenter {

    companion object {
        private const val BALANCE_CURRENCY = "USD"
    }

    init {
        launch {
            userInteractor.getTokensFlow().collect { tokens ->
                val balance = tokens
                    .map { it.price }
                    .fold(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_EVEN)

                view?.showData(tokens, balance)
            }
        }
    }

    override fun loadData() {
        launch {
            try {
                /**
                 * Loading tokens from remote is expensive, we are caching all data and fetching from remote on refresh
                 * */
                val tokens = userInteractor.getTokens()
                if (tokens.isNotEmpty()) {
                    val balance = tokens
                        .map { it.price }
                        .fold(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(2, RoundingMode.HALF_EVEN)
                    view?.showData(tokens, balance)
                    return@launch
                }

                view?.showLoading(true)
                userInteractor.loadTokens(BALANCE_CURRENCY)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading user data")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }

    override fun refresh() {
        view?.showRefreshing(true)
        launch {
            try {
                userInteractor.loadTokens(BALANCE_CURRENCY)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading user data")
                view?.showErrorMessage(e)
            } finally {
                view?.showRefreshing(false)
            }
        }
    }
}
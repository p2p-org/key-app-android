package com.p2p.wallet.main.ui.main

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.user.UserInteractor
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

    override fun loadData(isRefreshing: Boolean) {
        if (!isRefreshing) view?.showLoading(true)
        launch {
            try {
                val tokens = userInteractor.loadTokens(BALANCE_CURRENCY)
                val balance = tokens
                    .map { it.price }
                    .fold(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_EVEN)

                view?.showData(tokens, balance)
            } catch (e: Throwable) {
                Timber.e(e, "Error loading user data")
                view?.showErrorMessage(e)
            } finally {
                view?.showLoading(false)
            }
        }
    }
}
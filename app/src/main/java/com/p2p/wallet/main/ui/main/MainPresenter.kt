package com.p2p.wallet.main.ui.main

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.dashboard.model.local.Token
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
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

    private var job: Job? = null

    override fun loadData() {
        view?.showLoading(true)
        job?.cancel()
        job = launch {
            try {
                userInteractor.getTokensFlow().collect { tokens ->
                    if (tokens.isNotEmpty()) {
                        val balance = mapBalance(tokens)

                        view?.showData(tokens, balance)
                        view?.showLoading(false)
                        view?.showRefreshing(false)
                    }
                }
            } catch (e: CancellationException) {
                Timber.d("Loading tokens job cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Error loading user data")
                view?.showErrorMessage(e)
            }
        }
    }

    private fun mapBalance(tokens: List<Token>): BigDecimal =
        tokens
            .map { it.price }
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_EVEN)

    override fun refresh() {
        view?.showRefreshing(true)
        GlobalScope.launch {
            try {
                userInteractor.loadTokens(BALANCE_CURRENCY)
            } catch (e: CancellationException) {
                Timber.d("Loading tokens job cancelled")
            } catch (e: Throwable) {
                Timber.e(e, "Error loading user data")
                view?.showErrorMessage(e)
            } finally {
                view?.showRefreshing(false)
            }
        }
    }
}
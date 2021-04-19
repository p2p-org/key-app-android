package com.p2p.wallet.main.ui

import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.user.UserInteractor
import kotlinx.coroutines.launch
import timber.log.Timber

class MainPresenter(
    private val userInteractor: UserInteractor
) : BasePresenter<MainContract.View>(), MainContract.Presenter {

    override fun loadData(isRefreshing: Boolean) {
        if (!isRefreshing) view?.showLoading(true)
        launch {
            try {
                val balance = userInteractor.loadBalance()
                val tokens = userInteractor.loadTokens(balance)
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
package com.p2p.wallet.root.ui

import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class RootPresenter(
    private val authInteractor: AuthInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<RootContract.View>(), RootContract.Presenter {

    companion object {
        private const val BALANCE_CURRENCY = "USD"
    }

    init {
        launch {
            try {
                userInteractor.loadTokenPrices(BALANCE_CURRENCY)
                userInteractor.loadTokenBids()
            } catch (e: Throwable) {
                Timber.e(e, "Error loading initial data prices and bids")
            }
        }
    }

    override fun openRootScreen() {
        launch {
            withContext(Dispatchers.Default) {
                delay(500L)
                if (authInteractor.isAuthorized()) {
                    view?.navigateToSignIn()
                } else {
                    view?.navigateToOnboarding()
                }
            }
        }
    }
}
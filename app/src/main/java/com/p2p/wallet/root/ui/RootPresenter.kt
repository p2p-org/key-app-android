package com.p2p.wallet.root.ui

import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.launch

class RootPresenter(
    private val authInteractor: AuthInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<RootContract.View>(), RootContract.Presenter {

    companion object {
        private const val BALANCE_CURRENCY = "USD"
    }

    /**
     * Loading tokens is expensive operation, therefore we are starting it asap
     * */
    override fun openRootScreen() {
        launch {
            if (authInteractor.isAuthorized()) {
                view?.navigateToSignIn()
                userInteractor.loadTokens(BALANCE_CURRENCY)
            } else {
                view?.navigateToOnboarding()
            }
        }
    }
}
package com.p2p.wallet.root

import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.common.mvp.BasePresenter

class RootPresenter(
    private val authInteractor: AuthInteractor
) : BasePresenter<RootContract.View>(), RootContract.Presenter {

    override fun openRootScreen() {
        if (authInteractor.isAuthorized()) {
            view?.navigateToSignIn()
        } else {
            view?.navigateToOnboarding()
        }
    }
}
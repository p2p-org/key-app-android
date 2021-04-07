package com.p2p.wowlet.root

import com.p2p.wowlet.auth.AuthInteractor
import com.p2p.wowlet.common.mvp.BasePresenter

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
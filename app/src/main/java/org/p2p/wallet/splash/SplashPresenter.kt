package org.p2p.wallet.splash

import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

private const val MINIMUM_SPLASH_SHOWING_TIME_MS = 2000L

class SplashPresenter(
    private val authInteractor: AuthInteractor,
) : BasePresenter<SplashContract.View>(), SplashContract.Presenter {

    override fun attach(view: SplashContract.View) {
        super.attach(view)
        launch {
            delay(MINIMUM_SPLASH_SHOWING_TIME_MS)
            openRootScreen()
        }
    }

    private fun openRootScreen() {
        if (authInteractor.isAuthorized()) {
            view?.navigateToSignIn()
        } else {
            view?.navigateToOnboarding()
        }
    }
}

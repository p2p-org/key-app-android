package org.p2p.wallet.splash

import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MINIMUM_SPLASH_SHOWING_TIME_MS = 2000L

class SplashPresenter(
    private val authInteractor: AuthInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<SplashContract.View>(), SplashContract.Presenter {

    override fun attach(view: SplashContract.View) {
        super.attach(view)
        launch {
            delay(MINIMUM_SPLASH_SHOWING_TIME_MS)
            loadPricesAndBids()
        }
    }

    private fun loadPricesAndBids() {
        launch {
            try {
                userInteractor.loadAllTokensData()
            } catch (e: Throwable) {
                Timber.e(e, "Error loading initial tokens data")
            } finally {
                openRootScreen()
            }
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

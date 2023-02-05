package org.p2p.wallet.splash

import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.user.interactor.UserInteractor
import timber.log.Timber
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SplashPresenter(
    private val authInteractor: AuthInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<SplashContract.View>(), SplashContract.Presenter {

    override fun attach(view: SplashContract.View) {
        super.attach(view)
        launch {
            val allTokensData = initialLoading { userInteractor.loadAllTokensData() }
            allTokensData.join()
            openRootScreen()
        }
    }

    private fun initialLoading(action: suspend () -> Unit): Job {
        return launch {
            try {
                action()
            } catch (e: Throwable) {
                Timber.e(e, "Error loading initial tokens data")
            }
        }
    }

    private fun openRootScreen() {
        if (authInteractor.isAuthorized()) {
            view?.navigateToSignIn()
        } else {
            view?.navigateToOnboarding()
        }
        view?.hideSplashScreen()
    }
}

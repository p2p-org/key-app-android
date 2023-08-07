package org.p2p.wallet.splash

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.events.AppLoaderFacade
import org.p2p.wallet.user.interactor.TokenMetadataInteractor

class SplashPresenter(
    private val authInteractor: AuthInteractor,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val tokenMetadataInteractor: TokenMetadataInteractor,
    private val appLoaderFacade: AppLoaderFacade,
) : BasePresenter<SplashContract.View>(), SplashContract.Presenter {

    override fun attach(view: SplashContract.View) {
        super.attach(view)
        loadTokensList()
    }

    override fun logNotificationPermissionGranted(isGranted: Boolean) {
        onboardingAnalytics.setUserGrantedNotificationPermissions(isGranted = isGranted)
    }

    private fun loadTokensList() {
        launch {
            try {
                tokenMetadataInteractor.loadAllTokensMetadata()
            } catch (e: Throwable) {
                Timber.e(e, "Error loading initial tokens data")
            } finally {
                openRootScreen()
            }
        }
    }

    private fun openRootScreen() {
        launch {
            if (authInteractor.isAuthorized()) {
                launchAppLoaders()
                view?.navigateToSignIn()
            } else {
                view?.navigateToOnboarding()
            }
        }
    }

    private suspend fun launchAppLoaders() {
        appLoaderFacade.load()
    }
}

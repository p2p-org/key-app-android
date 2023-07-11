package org.p2p.wallet.splash

import timber.log.Timber
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.events.AppLoader
import org.p2p.wallet.tokenservice.TokenService
import org.p2p.wallet.user.interactor.BlockChainTokensMetadataInteractor

class SplashPresenter(
    private val authInteractor: AuthInteractor,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val blockchainTokensInteractor: BlockChainTokensMetadataInteractor,
    private val appLoader: AppLoader,
    private val tokenService: TokenService,
    private val appScope: AppScope
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
                blockchainTokensInteractor.loadAllTokensData()
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

    private suspend fun launchAppLoaders() = withContext(appScope.coroutineContext) {
        tokenService.onStart()
        appLoader.onLoad()
    }
}

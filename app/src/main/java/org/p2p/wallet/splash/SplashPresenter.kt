package org.p2p.wallet.splash

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.user.interactor.BlockChainTokensMetadataInteractor
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.user.interactor.UserTokensInteractor

class SplashPresenter(
    private val authInteractor: AuthInteractor,
    private val onboardingAnalytics: OnboardingAnalytics,
    private val blockchainTokensInteractor: BlockChainTokensMetadataInteractor
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
        if (authInteractor.isAuthorized()) {
            view?.navigateToSignIn()
        } else {
            view?.navigateToOnboarding()
        }
    }
}

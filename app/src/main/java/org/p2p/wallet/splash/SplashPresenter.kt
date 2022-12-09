package org.p2p.wallet.splash

import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRepository
import org.p2p.wallet.utils.toBase58Instance
import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MINIMUM_SPLASH_SHOWING_TIME_MS = 2000L

class SplashPresenter(
    private val authInteractor: AuthInteractor,
    private val sellInteractor: SellInteractor
) : BasePresenter<SplashContract.View>(), SplashContract.Presenter {

    override fun attach(view: SplashContract.View) {
        super.attach(view)
        launch {
            sellInteractor.loadSellAvailability()
            delay(MINIMUM_SPLASH_SHOWING_TIME_MS)

            val response = moonpaySellRepository.getUserSellTransactions(tokenKeyProvider.publicKey.toBase58Instance())
            Timber.i(response.toString())
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

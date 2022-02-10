package org.p2p.wallet.root

import android.content.Context
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import timber.log.Timber

class RootPresenter(
    private val authInteractor: AuthInteractor,
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val context: Context
) : BasePresenter<RootContract.View>(), RootContract.Presenter {

    companion object {
        private const val BALANCE_CURRENCY = "USD"
    }

    override fun attach(view: RootContract.View) {
        super.attach(view)
        val userId = tokenKeyProvider.publicKey
        if (userId.isNotEmpty()) {
            IntercomService.signIn(userId) { newMessagesCount ->
                if (newMessagesCount > 0) {
                    view?.showToast(context.getString(R.string.home_support_new_messages_count, newMessagesCount))
                }
            }
        }
    }

    override fun loadPricesAndBids() {
        launch {
            try {
                userInteractor.loadAllTokensData()
            } catch (e: Throwable) {
                Timber.e(e, "Error loading initial tokens data")
            }
        }
        launch {
            try {
                userInteractor.loadTokenPrices(BALANCE_CURRENCY)
            } catch (e: Throwable) {
                view?.showToast(R.string.error_rates_error)
                Timber.e(e, "Error loading initial tokens prices")
            }
        }
    }

    override fun openRootScreen() {
        if (authInteractor.isAuthorized()) {
            view?.navigateToSignIn()
        } else {
            view?.navigateToOnboarding()
        }
    }
}
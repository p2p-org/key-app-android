package org.p2p.wallet.auth.ui.onboarding.root

import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.web3authsdk.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class OnboardingRootPresenter(
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider,
    private val authInteractor: AuthInteractor
) : BasePresenter<OnboardingRootContract.View>(), OnboardingRootContract.Presenter {

    override fun attach(view: OnboardingRootContract.View) {
        super.attach(view)

        when {
            userLeftOnPinCreation() -> view.navigateToCreatePin()
            userSignUpDetailsStorage.getLastSignUpUserDetails() != null -> view.navigateToContinueOnboarding()
            else -> view.navigateToOnboarding()
        }
    }

    private fun userLeftOnPinCreation(): Boolean {
        return tokenKeyProvider.secretKey.isNotEmpty() && !authInteractor.isAuthorized()
    }
}

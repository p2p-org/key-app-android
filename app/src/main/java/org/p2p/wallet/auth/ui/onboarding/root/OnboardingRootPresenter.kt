package org.p2p.wallet.auth.ui.onboarding.root

import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.emptyString

class OnboardingRootPresenter(
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider,
    private val authInteractor: AuthInteractor
) : BasePresenter<OnboardingRootContract.View>(), OnboardingRootContract.Presenter {

    override fun attach(view: OnboardingRootContract.View) {
        super.attach(view)
        // pass empty string as UserId to launch IntercomService as anonymous user
        IntercomService.signIn(emptyString())

        when {
            userLeftOnPinCreation() -> view.navigateToCreatePin()
            userSignUpDetailsStorage.getLastSignUpUserDetails() != null -> {
                if (userSignUpDetailsStorage.isSignUpInProcess()) {
                    view.navigateToContinueOnboarding()
                } else {
                    view.navigateToRestore()
                }
            }
            else -> view.navigateToOnboarding()
        }

        // Sign in unidentified user for help messenger in onboarding flow
        IntercomService.signIn(emptyString())
    }

    private fun userLeftOnPinCreation(): Boolean {
        return tokenKeyProvider.secretKey.isNotEmpty() && !authInteractor.isAuthorized()
    }
}

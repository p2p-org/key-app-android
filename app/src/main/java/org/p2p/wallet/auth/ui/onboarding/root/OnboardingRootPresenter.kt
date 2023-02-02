package org.p2p.wallet.auth.ui.onboarding.root

import org.p2p.wallet.auth.analytics.OnboardingAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.emptyString

class OnboardingRootPresenter(
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider,
    private val authInteractor: AuthInteractor,
    private val onboardingAnalytics: OnboardingAnalytics
) : BasePresenter<OnboardingRootContract.View>(), OnboardingRootContract.Presenter {

    override fun attach(view: OnboardingRootContract.View) {
        super.attach(view)
        // pass empty string as UserId to launch IntercomService as anonymous user
        IntercomService.signIn(emptyString())

        val userDetails = userSignUpDetailsStorage.getLastSignUpUserDetails()
        onboardingAnalytics.setUserHasDeviceShare(
            hasDeviceShare = userDetails?.signUpDetails?.deviceShare != null
        )
        when {
            userLeftOnPinCreation() -> view.navigateToCreatePin()
            userDetails != null -> handleUserDetailsExist(userDetails)
            else -> view.navigateToOnboarding()
        }

        // Sign in unidentified user for help messenger in onboarding flow
        IntercomService.signIn(emptyString())
    }

    private fun handleUserDetailsExist(userDetails: UserSignUpDetailsStorage.SignUpUserDetails) {
        when {
            userSignUpDetailsStorage.isSignUpInProcess() -> view?.navigateToContinueOnboarding()
            userDetails.signUpDetails.deviceShare != null -> view?.navigateToRestore()
            else -> view?.navigateToOnboarding()
        }
    }

    private fun userLeftOnPinCreation(): Boolean {
        return tokenKeyProvider.keyPair.isNotEmpty() && !authInteractor.isAuthorized()
    }

    override fun logNotificationPermissionGranted(isGranted: Boolean) {
        onboardingAnalytics.setUserGrantedNotificationPermissions(isGranted = isGranted)
    }
}

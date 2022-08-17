package org.p2p.wallet.auth.ui.onboarding.root

import org.p2p.wallet.auth.web3authsdk.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.intercom.IntercomService
import org.p2p.wallet.utils.emptyString

class OnboardingRootPresenter(
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage
) : BasePresenter<OnboardingRootContract.View>(), OnboardingRootContract.Presenter {

    override fun attach(view: OnboardingRootContract.View) {
        super.attach(view)
        if (userSignUpDetailsStorage.getLastSignUpUserDetails() != null) {
            view.navigateToContinueOnboarding()
        } else {
            view.navigateToOnboarding()
        }

        // Sign in unidentified user for help messenger in onboarding flow
        IntercomService.signIn(emptyString())
    }
}

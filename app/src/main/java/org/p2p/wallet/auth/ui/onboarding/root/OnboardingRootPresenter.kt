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
        // pass empty string as UserId to launch IntercomService as anonymous user
        IntercomService.signIn(emptyString())

        if (userSignUpDetailsStorage.getLastSignUpUserDetails() != null) {
            view.navigateToContinueOnboarding()
        } else {
            view.navigateToOnboarding()
        }
    }
}

package org.p2p.wallet.auth.ui.onboarding.root

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingRootContract {
    interface View : MvpView {
        fun navigateToOnboarding()
        fun navigateToContinueOnboarding()
        fun navigateToRestore()
        fun navigateToCreatePin()
    }

    interface Presenter : MvpPresenter<View>
}

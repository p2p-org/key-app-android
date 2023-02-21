package org.p2p.wallet.auth.ui.onboarding.root

import android.net.Uri
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnboardingRootContract {
    interface View : MvpView {
        fun navigateToOnboarding()
        fun navigateToContinueOnboarding()
        fun navigateToRestore()
        fun navigateToCreatePin()
        fun navigateToMain()
    }

    interface Presenter : MvpPresenter<View> {
        fun validDeeplink(deeplink: Uri)
    }
}

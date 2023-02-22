package org.p2p.wallet.splash

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SplashContract {
    interface View : MvpView {
        fun navigateToOnboarding()
        fun navigateToSignIn()
    }

    interface Presenter : MvpPresenter<View> {
        fun logNotificationPermissionGranted(isGranted: Boolean)
    }
}

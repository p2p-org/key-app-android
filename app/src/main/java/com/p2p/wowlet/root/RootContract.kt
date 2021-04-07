package com.p2p.wowlet.root

import com.p2p.wowlet.common.mvp.MvpPresenter
import com.p2p.wowlet.common.mvp.MvpView

interface RootContract {

    interface View : MvpView {
        fun navigateToOnboarding()
        fun navigateToSignIn()
    }

    interface Presenter : MvpPresenter<View> {
        fun openRootScreen()
    }
}
package org.p2p.wallet.root.ui

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface RootContract {

    interface View : MvpView {
        fun navigateToOnboarding()
        fun navigateToSignIn()
    }

    interface Presenter : MvpPresenter<View> {
        fun openRootScreen()
        fun loadPricesAndBids()
    }
}
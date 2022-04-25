package org.p2p.wallet.root

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface RootContract {

    interface View : MvpView {
        fun navigateToOnboarding()
        fun navigateToSignIn()
        fun showToast(@StringRes message: Int)
        fun showToast(message: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun openRootScreen()
        fun loadPricesAndBids()
    }
}

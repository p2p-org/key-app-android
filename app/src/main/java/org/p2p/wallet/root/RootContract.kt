package org.p2p.wallet.root

import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.deeplinks.DeeplinkScreenNavigator

interface RootContract {

    interface View : MvpView, DeeplinkScreenNavigator {
        fun showToast(@StringRes message: Int)
        fun showToast(message: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun observeDeeplinks()
    }
}

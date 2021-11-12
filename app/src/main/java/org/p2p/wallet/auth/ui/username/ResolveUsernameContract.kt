package org.p2p.wallet.auth.ui.username

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ResolveUsernameContract {

    interface View : MvpView {
        fun showNetworkSelection()
    }

    interface Presenter : MvpPresenter<View> {
        fun send()
    }

}
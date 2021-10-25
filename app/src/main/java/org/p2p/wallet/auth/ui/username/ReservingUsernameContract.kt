package org.p2p.wallet.auth.ui.username

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ReservingUsernameContract {

    interface View : MvpView {
        fun navigateToPinCode()
    }

    interface Presenter : MvpPresenter<View> {
        fun checkUsername(username: String)
        fun registerUsername()
    }
}
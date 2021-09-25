package com.p2p.wallet.auth.ui.username

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView

interface ReservingUsernameContract {

    interface View : MvpView {
        fun navigateToSecurityKey()
    }

    interface Presenter : MvpPresenter<View> {
        fun checkUsername()
        fun createUsername()
    }
}
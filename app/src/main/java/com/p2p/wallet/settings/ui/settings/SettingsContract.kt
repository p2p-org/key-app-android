package com.p2p.wallet.settings.ui.settings

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView

interface SettingsContract {

    interface View : MvpView {
        fun showHiddenBalance(isHidden: Boolean)
        fun showAuthorization()
        fun showUsername(username: String?)
        fun openUsernameScreen()
        fun openReserveUsernameScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun setZeroBalanceHidden(isHidden: Boolean)
        fun logout()
        fun checkUsername(): Boolean
    }
}
package org.p2p.wallet.settings.ui.settings

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.settings.model.SettingsRow

interface SettingsContract {

    interface View : MvpView {
        fun showSettings(item: List<SettingsRow>)
        fun showReserveUsername()
        fun showUsername()
        fun showLogoutConfirm()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun logout()
        fun onUsernameClicked()
        fun onNetworkChanged(newName: String)
        fun onZeroBalanceVisibilityChanged(isVisible: Boolean)
        fun onLogoutClicked()
    }
}

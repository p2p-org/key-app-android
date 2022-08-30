package org.p2p.wallet.settings.ui.settings

import org.p2p.solanaj.rpc.NetworkEnvironment
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem

interface SettingsContract {
    interface View : MvpView {
        fun showSettings(settings: List<SettingsItem>)
        fun showSignOutConfirmDialog()
        fun openUsernameScreen()
        fun openReserveUsernameScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun onUsernameSettingClicked()
        fun changeZeroBalanceHiddenFlag(hideValue: Boolean)
        fun changeBiometricConfirmFlag(isBiometricConfirmNeeded: Boolean)
        fun onSignOutClicked()
        fun onConfirmSignOutClicked()
        fun onNetworkEnvironmentChanged(newNetworkEnvironment: NetworkEnvironment)
    }
}

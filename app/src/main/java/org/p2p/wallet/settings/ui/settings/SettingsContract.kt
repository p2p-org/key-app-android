package org.p2p.wallet.settings.ui.settings

import androidx.annotation.StringRes
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.settings.model.SettingItem

interface SettingsContract {

    interface View : MvpView {
        fun showProfile(items: List<SettingItem>)
        fun showNetwork(items: List<SettingItem>)
        fun onProfileItemClicked(@StringRes titleRes: Int)
        fun onNetworkItemClicked(@StringRes titleRes: Int)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()

    }
}
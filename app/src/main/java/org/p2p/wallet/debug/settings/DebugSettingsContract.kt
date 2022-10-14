package org.p2p.wallet.debug.settings

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment
import org.p2p.wallet.settings.model.SettingsRow

interface DebugSettingsContract {

    interface View : MvpView {
        fun showSettings(item: List<SettingsRow>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun onNetworkChanged(newNetworkEnvironment: NetworkEnvironment)
    }
}

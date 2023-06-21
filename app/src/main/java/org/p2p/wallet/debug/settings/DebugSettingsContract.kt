package org.p2p.wallet.debug.settings

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.wallet.settings.model.SettingsRow

interface DebugSettingsContract {

    interface View : MvpView {
        fun showSettings(items: List<SettingsRow>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun onNetworkChanged(newNetworkEnvironment: NetworkEnvironment)
        fun switchMoonpayUrl(isSandboxSelected: Boolean)
        fun switchNameServiceUrl(isProdSelected: Boolean)
        fun onSettingsPopupMenuClicked(selectedValue: String)
    }
}

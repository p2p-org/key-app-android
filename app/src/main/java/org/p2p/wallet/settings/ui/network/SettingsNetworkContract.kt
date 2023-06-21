package org.p2p.wallet.settings.ui.network

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.core.network.environment.NetworkEnvironment

interface SettingsNetworkContract {

    interface View : MvpView {
        fun showEnvironment(currentNetwork: NetworkEnvironment, availableNetworks: List<NetworkEnvironment>)
        fun closeWithResult(newNetworkEnvironment: NetworkEnvironment)
        fun dismissBottomSheet()
    }

    interface Presenter : MvpPresenter<View> {
        fun onNewEnvironmentSelected(newNetwork: NetworkEnvironment)
    }
}

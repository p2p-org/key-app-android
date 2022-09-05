package org.p2p.wallet.settings.ui.network

import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SettingsNetworkContract {

    interface View : MvpView {
        fun showEnvironment(currentNetwork: NetworkEnvironment, availableNetworks: List<NetworkEnvironment>)
        fun closeWithResult(newNetworkEnvironment: NetworkEnvironment)
        fun navigateBack()
    }

    interface Presenter : MvpPresenter<View> {
        fun onNewEnvironmentSelected(newNetwork: NetworkEnvironment)
        fun confirmNetworkEnvironmentSelected()
    }
}

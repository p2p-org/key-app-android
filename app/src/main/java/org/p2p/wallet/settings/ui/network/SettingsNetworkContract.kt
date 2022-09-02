package org.p2p.wallet.settings.ui.network

import org.p2p.solanaj.rpc.NetworkEnvironment
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SettingsNetworkContract {

    interface View : MvpView {
        fun showEnvironment(
            currentNetwork: NetworkEnvironment,
            availableNetworks: List<NetworkEnvironment>,
            isDevnetEnabled: Boolean
        )

        fun closeWithResult(newNetworkEnvironment: NetworkEnvironment)
        fun close()
    }

    interface Presenter : MvpPresenter<View> {
        fun onNewEnvironmentSelected(newNetwork: NetworkEnvironment)
        fun confirmNetworkEnvironmentSelected()
    }
}

package org.p2p.wallet.settings.ui.network

import android.widget.RadioGroup
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.solanaj.rpc.NetworkEnvironment

interface SettingsNetworkContract {

    interface View : MvpView, RadioGroup.OnCheckedChangeListener {
        fun showEnvironment(
            currentNetwork: NetworkEnvironment,
            availableNetworks: List<NetworkEnvironment>,
            isDevnetEnabled: Boolean
        )
        fun onNetworkChanged(newName: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun save()
        fun setNewEnvironment(newNetwork: NetworkEnvironment)
    }
}

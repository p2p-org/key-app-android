package org.p2p.wallet.settings.ui.network

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.solanaj.rpc.Environment

interface SettingsNetworkContract {

    interface View : MvpView {
        fun showEnvironment(environment: Environment, isDevnetEnabled: Boolean)
        fun onNetworkChanged(newName: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun save()
        fun setNewEnvironment(environment: Environment)
    }
}

package org.p2p.wallet.settings.ui.network

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.solanaj.rpc.Environment

interface NetworkContract {

    interface View : MvpView {
        fun showEnvironment(environment: Environment)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun setNewEnvironment(environment: Environment)
    }
}
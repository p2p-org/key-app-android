package org.p2p.wallet.main.ui.receive.network

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.main.model.NetworkType

interface ReceiveNetworkTypeContract {
    interface View : MvpView {
        fun showNetworkInfo(type: NetworkType)
        fun setCheckState(type: NetworkType)
        fun navigateToReceive(type: NetworkType)
    }

    interface Presenter : MvpPresenter<View> {
        fun onNetworkChanged(type: NetworkType)
        fun load()
    }
}
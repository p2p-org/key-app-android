package org.p2p.wallet.receive.network

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.send.model.NetworkType
import java.math.BigDecimal

interface ReceiveNetworkTypeContract {
    interface View : MvpView {
        fun showNetworkInfo(type: NetworkType)
        fun setCheckState(type: NetworkType)
        fun navigateToReceive(type: NetworkType)
        fun showLoading(isLoading: Boolean)
        fun showBuy(priceInSol: BigDecimal, priceInUsd: BigDecimal?, type: NetworkType)
        fun showTopup()
    }

    interface Presenter : MvpPresenter<View> {
        fun onNetworkChanged(type: NetworkType)
        fun load()
    }
}
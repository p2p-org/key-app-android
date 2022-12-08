package org.p2p.wallet.receive.network

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.core.token.Token
import org.p2p.wallet.send.model.NetworkType
import java.math.BigDecimal

interface ReceiveNetworkTypeContract {
    interface View : MvpView {
        fun showNetworkInfo(type: NetworkType)
        fun setCheckState(type: NetworkType)
        fun navigateToReceive(type: NetworkType)
        fun showLoading(isLoading: Boolean)
        fun showBuy(priceInSol: BigDecimal, priceInUsd: BigDecimal?)
        fun showCreateByFeeRelay()
        fun showTokensForBuy(tokens: List<Token>)
        fun showTopup()
        fun close()
        fun showNewBuyFragment(token: Token)
    }

    interface Presenter : MvpPresenter<View> {
        fun onNetworkChanged(networkType: NetworkType)
        fun onTopupSelected(isSelected: Boolean)
        fun onBuySelected(isSelected: Boolean)
        fun onBtcSelected(isSelected: Boolean)
        fun load()
    }
}

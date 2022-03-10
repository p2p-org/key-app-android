package org.p2p.wallet.renbtc.ui.buy

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.model.Token

interface RenBtcBuyContract {
    interface View : MvpView {
        fun showTokensForBuy(tokens: List<Token>)
    }

    interface Presenter : MvpPresenter<View> {
        fun onBuyClicked()
    }
}
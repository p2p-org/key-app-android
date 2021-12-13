package org.p2p.wallet.main.ui.buy.moonpay

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.main.model.BuyData

interface BuySolanaContract {

    interface View : MvpView {
        fun showData(data: BuyData)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadSolData()
        fun setBuyAmount(amount: String)
    }
}
package org.p2p.wallet.moonpay.ui.new

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.model.Token

interface NewBuyContract {

    interface View : MvpView {
        fun initTokensToBuy(tokensToBuy: List<Token>)
    }

    interface Presenter : MvpPresenter<View>
}

package com.p2p.wallet.main.ui.buy

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView

interface BuyContract {

    interface View : MvpView {
        fun openWebView(url: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
    }
}
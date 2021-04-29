package com.p2p.wallet.main.ui.receive

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView

interface ReceiveContract {

    interface View : MvpView {
        fun showAddress(address: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
    }
}
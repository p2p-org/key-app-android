package com.p2p.wallet.main.ui

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.dashboard.model.local.Token

interface MainContract {

    interface View : MvpView {
        fun showData(tokens: List<Token>, balance: Long)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData(isRefreshing: Boolean)
    }
}
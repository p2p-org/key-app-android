package com.p2p.wallet.main.ui.main

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.main.model.TokenItem
import com.p2p.wallet.token.model.Token
import java.math.BigDecimal

interface MainContract {

    interface View : MvpView {
        fun showData(tokens: List<TokenItem>, balance: BigDecimal)
        fun showLoading(isLoading: Boolean)
        fun showRefreshing(isRefreshing: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun refresh()
        fun toggleVisibility(token: Token)
    }
}
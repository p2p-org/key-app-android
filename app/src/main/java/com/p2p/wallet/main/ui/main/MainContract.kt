package com.p2p.wallet.main.ui.main

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.main.model.TokenItem
import com.p2p.wallet.main.model.VisibilityState
import com.p2p.wallet.main.model.Token
import java.math.BigDecimal

interface MainContract {

    interface View : MvpView {
        fun showTokens(tokens: List<TokenItem>, isZerosHidden: Boolean, state: VisibilityState)
        fun showBalance(balance: BigDecimal)
        fun showChart(tokens: List<Token>)
        fun showLoading(isLoading: Boolean)
        fun showRefreshing(isRefreshing: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun collectData()
        fun refresh()
        fun toggleVisibility(token: Token)
        fun toggleVisibilityState()
        fun clearCache()
    }
}
package org.p2p.wallet.main.ui.main

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.main.model.TokenItem
import org.p2p.wallet.main.model.VisibilityState
import org.p2p.wallet.main.model.Token
import java.math.BigDecimal

interface MainContract {

    interface View : MvpView {
        fun showTokens(tokens: List<TokenItem>, isZerosHidden: Boolean, state: VisibilityState)
        fun showBalance(balance: BigDecimal)
        fun showChart(tokens: List<Token.Active>)
        fun showLoading(isLoading: Boolean)
        fun showRefreshing(isRefreshing: Boolean)
        fun showUsernameBanner(isVisible: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun collectData()
        fun refresh()
        fun toggleVisibility(token: Token.Active)
        fun toggleVisibilityState()
        fun clearCache()
        fun hideUsernameBanner()
    }
}
package org.p2p.wallet.home.ui.main

import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.widget.ActionButtonsView
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.model.VisibilityState
import org.p2p.wallet.home.ui.main.adapter.OnHomeItemsClickListener
import java.math.BigDecimal

interface HomeContract {

    interface View : MvpView, OnHomeItemsClickListener {
        fun showTokens(tokens: List<HomeElementItem>, isZerosHidden: Boolean, state: VisibilityState)
        fun showTokensForBuy(tokens: List<Token>)
        fun showBalance(balance: BigDecimal, username: Username?)
        fun showActions(items: List<ActionButtonsView.ActionButton>)
        fun showRefreshing(isRefreshing: Boolean)
        fun showEmptyState(isEmpty: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun onBuyClicked()
        fun subscribeToUserTokensFlow()
        fun refreshTokenAndPrices()
        fun toggleTokenVisibility(token: Token.Active)
        fun toggleTokenVisibilityState()
        fun clearTokensCache()
    }
}

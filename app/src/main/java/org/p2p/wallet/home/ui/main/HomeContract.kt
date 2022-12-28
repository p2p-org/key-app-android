package org.p2p.wallet.home.ui.main

import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.ui.main.adapter.OnHomeItemsClickListener
import java.math.BigDecimal

interface HomeContract {

    interface View : MvpView, OnHomeItemsClickListener {
        fun showTokens(tokens: List<HomeElementItem>, isZerosHidden: Boolean)
        fun showTokensForBuy(tokens: List<Token>)
        fun showNewBuyScreen(token: Token)
        fun showOldBuyScreen(token: Token)
        fun showBalance(balance: BigDecimal)
        fun showRefreshing(isRefreshing: Boolean)
        fun showEmptyViewData(data: List<Any>)
        fun showEmptyState(isEmpty: Boolean)
        fun showUserAddress(ellipsizedAddress: String)
        fun showNewSendScreen()

        fun navigateToProfile()
        fun navigateToReserveUsername()
        fun showAddressCopied(addressAndUsername: String)
        fun showBuyInfoScreen(token: Token)

        fun showSendNoTokens(fallbackToken: Token)
        fun setSellActionButtonIsVisible(isVisible: Boolean)
        fun setSwapActionButtonIsVisible(isVisible: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun onBuyClicked()
        fun onSendClicked()
        fun onBuyTokenClicked(token: Token)
        fun onInfoBuyTokenClicked(token: Token)
        fun refreshTokens()
        fun toggleTokenVisibility(token: Token.Active)
        fun toggleTokenVisibilityState()
        fun clearTokensCache()
        fun onProfileClick()
        fun onAddressClicked()

        fun updateTokensIfNeeded()
        fun load()
    }
}

package org.p2p.wallet.home.ui.main

import androidx.lifecycle.DefaultLifecycleObserver
import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.ui.main.adapter.OnHomeItemsClickListener

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
        fun showActionButtons(buttons: List<ActionButton>)

        fun navigateToProfile()
        fun navigateToReserveUsername()
        fun showAddressCopied(addressAndUsername: String)
        fun showBuyInfoScreen(token: Token)

        fun showSendNoTokens(fallbackToken: Token)
    }

    interface Presenter : MvpPresenter<View>, DefaultLifecycleObserver {
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

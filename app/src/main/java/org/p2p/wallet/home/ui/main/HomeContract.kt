package org.p2p.wallet.home.ui.main

import androidx.lifecycle.DefaultLifecycleObserver
import org.p2p.core.token.Token
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.ui.main.adapter.OnHomeItemsClickListener
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.transaction.model.NewShowProgress

interface HomeContract {

    interface View : MvpView, OnHomeItemsClickListener {
        fun showTokens(tokens: List<HomeElementItem>, isZerosHidden: Boolean)
        fun showTokensForBuy(tokens: List<Token>)
        fun showNewBuyScreen(token: Token, fiatToken: String? = null, fiatAmount: String? = null)
        fun showOldBuyScreen(token: Token)
        fun showBalance(cellModel: TextViewCellModel?)
        fun showRefreshing(isRefreshing: Boolean)
        fun showEmptyViewData(data: List<Any>)
        fun showEmptyState(isEmpty: Boolean)
        fun showUserAddress(ellipsizedAddress: String)
        fun showNewSendScreen(openedFromScreen: SearchOpenedFromScreen)
        fun showActionButtons(buttons: List<ActionButton>)
        fun showSwapWithArgs(tokenASymbol: String, tokenBSymbol: String, amountA: String, source: SwapOpenedFrom)
        fun showSwap(source: SwapOpenedFrom)
        fun showCashOut()

        fun navigateToProfile()
        fun navigateToReserveUsername()
        fun showAddressCopied(addressOrUsername: String)
        fun showBuyInfoScreen(token: Token)

        fun showSendNoTokens(fallbackToken: Token)
        fun showTokenClaim(token: Token.Eth)

        fun showProgressDialog(bundleId: String, progressDetails: NewShowProgress)
    }

    interface Presenter : MvpPresenter<View>, DefaultLifecycleObserver {
        fun onBuyClicked()
        fun onSendClicked(clickSource: SearchOpenedFromScreen)
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
        fun onClaimClicked(canBeClaimed: Boolean, token: Token.Eth)
    }
}

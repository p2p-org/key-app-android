package org.p2p.wallet.home.ui.crypto

import androidx.annotation.StringRes
import org.p2p.core.token.Token
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.transaction.model.NewShowProgress

interface MyCryptoContract {

    interface View : MvpView {
        fun showAddressCopied(addressOrUsername: String, @StringRes stringResId: Int)
        fun showUserAddress(ellipsizedAddress: String)
        fun showBalance(cellModel: TextViewCellModel?)
        fun showBalancePnl(cellModel: TextViewCellModel?)
        fun showPnlDetails(showPnlPercentage: String)
        fun hideBalancePnl()
        fun showRefreshing(isRefreshing: Boolean)
        fun showActionButtons(buttons: List<ActionButton>)
        fun showTokenHistory(token: Token.Active)
        fun showItems(items: List<AnyCellItem>)
        fun showEmptyState(isEmpty: Boolean)
        fun navigateToReceive()
        fun showAddMoneyDialog()
        fun navigateToSend()
        fun navigateToSwap()
        fun navigateToSell()
        fun navigateToTokenClaim(token: Token.Eth)
        fun showProgressDialog(bundleId: String, progressDetails: NewShowProgress)
    }

    interface Presenter : MvpPresenter<View> {
        fun refreshTokens()
        fun onTokenClicked(token: Token.Active)
        fun onAmountClicked()
        fun onBuyClicked()
        fun onReceiveClicked()
        fun onSendClicked()
        fun onAddressClicked()
        fun onSwapClicked()
        fun onSellClicked()
        fun toggleTokenVisibility(token: Token.Active)
        fun toggleTokenVisibilityState()
        fun onClaimClicked(canBeClaimed: Boolean, token: Token.Eth)
        fun onBalancePnlClicked()
    }
}

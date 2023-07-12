package org.p2p.wallet.home.ui.crypto

import org.p2p.core.token.Token
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.transaction.model.NewShowProgress

interface MyCryptoContract {

    interface View : MvpView {
        fun showBalance(cellModel: TextViewCellModel?)
        fun showRefreshing(isRefreshing: Boolean)
        fun showActionButtons(buttons: List<ActionButton>)
        fun showItems(items: List<AnyCellItem>)
        fun showEmptyState(isEmpty: Boolean)
        fun navigateToReceive()
        fun navigateToSwap()
        fun navigateToTokenClaim(token: Token.Eth)
        fun showProgressDialog(bundleId: String, progressDetails: NewShowProgress)
    }

    interface Presenter : MvpPresenter<View> {
        fun refreshTokens()
        fun onReceiveClicked()
        fun onSwapClicked()
        fun toggleTokenVisibility(token: Token.Active)
        fun toggleTokenVisibilityState()
        fun onClaimClicked(canBeClaimed: Boolean, token: Token.Eth)
    }
}

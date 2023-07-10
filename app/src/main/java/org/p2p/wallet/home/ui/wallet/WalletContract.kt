package org.p2p.wallet.home.ui.wallet

import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.newsend.ui.SearchOpenedFromScreen

interface WalletContract {

    interface View : MvpView {
        fun showBalance(cellModel: TextViewCellModel?)
        fun showRefreshing(isRefreshing: Boolean)
        fun showEmptyViewData(data: List<Any>)
        fun showEmptyState(isEmpty: Boolean)
        fun showUserAddress(ellipsizedAddress: String)
        fun showActionButtons(buttons: List<ActionButton>)
        fun navigateToProfile()
        fun navigateToReserveUsername()
        fun showAddressCopied(addressOrUsername: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun refreshTokens()
        fun onBuyClicked()
        fun onSellClicked()
        fun onSwapClicked()
        fun onTopupClicked()
        fun onSendClicked(clickSource: SearchOpenedFromScreen)
        fun onProfileClick()
        fun onAddressClicked()
    }
}

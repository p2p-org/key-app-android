package org.p2p.wallet.sell.ui.payload

import org.p2p.uikit.components.SellWidgetViewState
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails

interface SellPayloadContract {
    data class ViewState(
        val cashOutButtonState: CashOutButtonState,
        val widgetViewState: SellWidgetViewState
    )

    interface View : MvpView {
        fun showLoading(isVisible: Boolean)
        fun showButtonLoading(isLoading: Boolean)
        fun showMoonpayWidget(url: String)
        fun navigateToSellLock(details: SellTransactionViewDetails, isTransactionJustCreated: Boolean)
        fun navigateToErrorScreen()
        fun navigateToInformationScreen()
        fun updateViewState(newState: ViewState)
        fun updateToolbarTitle(tokenSymbol: String)
        fun setButtonState(state: CashOutButtonState)
        fun showOnlySolWarning()
        fun showKeyboard()
    }

    interface Presenter : MvpPresenter<View> {
        fun cashOut()
        fun onTokenAmountChanged(newValue: String)
        fun onUserMaxClicked()
        fun switchCurrencyMode()
        fun checkSellLock()
        fun setNeedCheckForSellLock()
        fun buildMoonpayWidget()
    }
}

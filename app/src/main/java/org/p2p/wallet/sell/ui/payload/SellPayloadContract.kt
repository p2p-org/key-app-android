package org.p2p.wallet.sell.ui.payload

import org.p2p.uikit.components.SellWidgetViewState
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import java.math.BigDecimal

interface SellPayloadContract {
    data class ViewState(
        val cashOutButtonState: CashOutButtonState,
        val widgetViewState: SellWidgetViewState
    )

    interface View : MvpView {
        fun showLoading(isVisible: Boolean)
        fun showMoonpayWidget(url: String)
        fun navigateToSellLock(details: SellTransactionViewDetails)
        fun navigateToErrorScreen()
        fun navigateNotEnoughTokensErrorScreen(minAmount: BigDecimal)
        fun updateViewState(newState: ViewState)
        fun setButtonState(state: CashOutButtonState)
    }

    interface Presenter : MvpPresenter<View> {
        fun cashOut()
        fun onTokenAmountChanged(newValue: String)
        fun onUserMaxClicked()
        fun switchCurrencyMode()
    }
}

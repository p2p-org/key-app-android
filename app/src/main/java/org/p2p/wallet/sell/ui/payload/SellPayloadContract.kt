package org.p2p.wallet.sell.ui.payload

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal

interface SellPayloadContract {

    interface View : MvpView {
        fun showLoading(isVisible: Boolean)
        fun showAvailableSolToSell(totalAmount: BigDecimal)
        fun setMinSolToSell(minAmount: BigDecimal, tokenSymbol: String)
        fun showMoonpayWidget(url: String)
        fun navigateToSellLock(solAmount: BigDecimal, usdAmount: String, moonpayAddress: Base58String)
        fun showErrorScreen()
        fun showNotEnoughMoney(minAmount: BigDecimal)
        fun updateViewState(newState: ViewState)
        fun setButtonState(state: CashOutButtonState)
        fun setTokenAmount(newValue: String)
        fun setFiatAndFeeValue(newValue: String)
        fun setTokenAndFeeValue(newValue: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun cashOut()
        fun onTokenAmountChanged(newValue: String)
        fun onCurrencyAmountChanged(newValue: String)
        fun onUserMaxClicked()
    }
}

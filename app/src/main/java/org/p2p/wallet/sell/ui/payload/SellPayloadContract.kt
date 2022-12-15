package org.p2p.wallet.sell.ui.payload

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import java.math.BigDecimal

interface SellPayloadContract {

    interface View : MvpView {
        fun showLoading(isVisible: Boolean)
        fun navigateToSellLock()
        fun showErrorScreen()
        fun showNotEnoughMoney(minAmount: BigDecimal)
        fun updateValues(
            quoteAmount: String,
            fee: String,
            fiat: String,
            minSolToSell: String,
            tokenSymbol: String,
            fiatSymbol: String,
            userBalance: String
        )
        fun setButtonState(state: ButtonState)
        fun setTokenAmount(newValue: String)
        fun reset()
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun cashOut()
        fun onTokenAmountChanged(newValue: String)
        fun onCurrencyAmountChanged(newValue: String)
        fun onUserMaxClicked()
    }
}

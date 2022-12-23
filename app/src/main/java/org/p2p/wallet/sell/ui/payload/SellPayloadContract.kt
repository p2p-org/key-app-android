package org.p2p.wallet.sell.ui.payload

import androidx.annotation.ColorRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.sell.ui.lock.SellTransactionDetails
import java.math.BigDecimal

interface SellPayloadContract {

    data class CashOutButtonState(
        val isEnabled: Boolean,
        @ColorRes val backgroundColor: Int,
        @ColorRes val textColor: Int,
        val text: String
    )

    data class ViewState(
        val quoteAmount: String,
        val fee: String,
        val fiat: String,
        val solToSell: String,
        val tokenSymbol: String,
        val fiatSymbol: String,
        val userBalance: String
    )

    interface View : MvpView {
        fun showLoading(isVisible: Boolean)
        fun showAvailableSolToSell(totalAmount: BigDecimal)
        fun setMinSolToSell(minAmount: BigDecimal, tokenSymbol: String)
        fun showMoonpayWidget(url: String)
        fun navigateToSellLock(details: SellTransactionDetails)
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

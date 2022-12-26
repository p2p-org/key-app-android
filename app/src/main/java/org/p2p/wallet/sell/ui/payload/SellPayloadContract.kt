package org.p2p.wallet.sell.ui.payload

import androidx.annotation.ColorRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import java.math.BigDecimal

interface SellPayloadContract {

    data class CashOutButtonState(
        val isEnabled: Boolean,
        @ColorRes val backgroundColor: Int,
        @ColorRes val textColor: Int,
        val text: String
    )

    data class ViewState(
        val formattedFiatAmount: String,
        val formattedSellFiatFee: String,
        val formattedTokenPrice: String,
        val solToSell: String,
        val tokenSymbol: String,
        val fiatSymbol: String,
        val formattedUserAvailableBalance: String
    )

    interface View : MvpView {
        fun showLoading(isVisible: Boolean)
        fun showAvailableSolToSell(totalAmount: BigDecimal)
        fun setMinSolToSell(minAmount: BigDecimal, tokenSymbol: String)
        fun showMoonpayWidget(url: String)
        fun navigateToSellLock(details: SellTransactionViewDetails)
        fun navigateToErrorScreen()
        fun showNotEnoughMoney(minAmount: BigDecimal)
        fun updateViewState(newState: ViewState)
        fun setButtonState(state: CashOutButtonState)
        fun setTokenAmount(newValue: String)
        fun setTokenAndFeeValue(newValue: String)
        fun resetFiatAndFee(feeSymbol: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun cashOut()
        fun onTokenAmountChanged(newValue: String)
        fun onUserMaxClicked()
    }
}

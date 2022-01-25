package org.p2p.wallet.swap.ui.serum

import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.ui.transaction.TransactionInfo
import org.p2p.wallet.swap.model.serum.SerumAmountData
import org.p2p.wallet.swap.model.PriceData
import org.p2p.wallet.swap.model.Slippage
import java.math.BigDecimal

interface SerumSwapContract {

    interface View : MvpView {
        fun showSourceToken(token: Token)
        fun showDestinationToken(token: Token?)
        fun showSourceAvailable(available: String)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showLoading(isLoading: Boolean)
        fun showFees(networkFee: String, liquidityFee: String, feeOption: String)
        fun showPrice(priceData: PriceData)
        fun hidePrice()
        fun showCalculations(data: SerumAmountData)
        fun hideCalculations()
        fun setAvailableTextColor(@AttrRes availableColor: Int)
        fun showAroundValue(aroundValue: BigDecimal)
        fun showButtonEnabled(isEnabled: Boolean)
        fun showSwapSuccess(info: TransactionInfo)
        fun showSlippage(slippage: Slippage)
        fun showButtonText(@StringRes textRes: Int, vararg value: String)
        fun setNewAmount(sourceAmount: String)
        fun updateInputValue(available: BigDecimal)
        fun openSourceSelection(tokens: List<Token.Active>)
        fun openDestinationSelection(tokens: List<Token>)
        fun openSwapSettings(currentSlippage: Slippage)
        fun openSlippageDialog(currentSlippage: Slippage)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadInitialData()
        fun loadTokensForSourceSelection()
        fun loadTokensForDestinationSelection()
        fun loadDataForSwapSettings()
        fun loadSlippage()
        fun setNewSourceToken(newToken: Token.Active)
        fun setNewDestinationToken(newToken: Token)
        fun setSourceAmount(amount: String)
        fun setSlippage(slippage: Slippage)
        fun swap()
        fun feedAvailableValue()
        fun reverseTokens()
    }
}
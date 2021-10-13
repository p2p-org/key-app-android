package com.p2p.wallet.swap.orca.ui

import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.swap.model.PriceData
import com.p2p.wallet.swap.model.Slippage
import com.p2p.wallet.swap.orca.model.OrcaAmountData
import com.p2p.wallet.swap.orca.model.OrcaFeeData
import java.math.BigDecimal

interface OrcaSwapContract {

    interface View : MvpView {
        fun showSourceToken(token: Token.Active)
        fun showDestinationToken(token: Token?)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showLoading(isLoading: Boolean)
        fun showFees(data: OrcaFeeData)
        fun showPrice(priceData: PriceData)
        fun hidePrice()
        fun showCalculations(data: OrcaAmountData?)
        fun hideCalculations()
        fun setAvailableTextColor(@AttrRes availableColor: Int)
        fun showNewAmount(amount: String)
        fun showAroundValue(aroundValue: BigDecimal)
        fun showButtonEnabled(isEnabled: Boolean)
        fun showSwapSuccess(info: TransactionInfo)
        fun showSlippage(slippage: Slippage)
        fun showButtonText(@StringRes textRes: Int, value: String? = null)
        fun setNewAmount(sourceAmount: String)
        fun openSourceSelection(tokens: List<Token.Active>)
        fun openDestinationSelection(tokens: List<Token>)
        fun openSwapSettings(currentSlippage: Slippage)
        fun openSlippageDialog(currentSlippage: Slippage)
        fun showError(@StringRes errorText: Int?)
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
package com.p2p.wallet.swap.ui

import androidx.annotation.ColorRes
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.swap.model.Slippage
import com.p2p.wallet.token.model.Token
import java.math.BigDecimal

interface SwapContract {

    interface View : MvpView {
        fun showSourceToken(token: Token)
        fun showDestinationToken(token: Token)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showLoading(isLoading: Boolean)
        fun showPrice(amount: BigDecimal, exchangeToken: String, perToken: String)
        fun showCalculations(data: CalculationsData)
        fun setAvailableTextColor(@ColorRes availableColor: Int)
        fun showAroundValue(aroundValue: BigDecimal)
        fun showButtonEnabled(isEnabled: Boolean)
        fun showSwapSuccess()
        fun updateInputValue(available: BigDecimal)
        fun openSourceSelection(tokens: List<Token>)
        fun openDestinationSelection(tokens: List<Token>)
        fun openSlippageSelection(currentSlippage: Slippage)
        fun showNoPoolFound()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadInitialData()
        fun loadTokensForSourceSelection()
        fun loadTokensForDestinationSelection()
        fun loadSlippageForSelection()
        fun setNewSourceToken(newToken: Token)
        fun setNewDestinationToken(newToken: Token)
        fun setSourceAmount(amount: BigDecimal)
        fun setSlippage(slippage: Double)
        fun swap()
        fun loadPrice(toggle: Boolean)
        fun feedAvailableValue()
    }
}
package com.p2p.wallet.swap.swap

import androidx.annotation.ColorRes
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.token.model.Token
import java.math.BigDecimal
import java.math.BigInteger

interface SwapContract {

    interface View : MvpView {
        fun showSourceToken(token: Token)
        fun showDestinationToken(token: Token)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showLoading(isLoading: Boolean)
        fun showPrice(destinationAmount: BigInteger, exchangeToken: String, perToken: String)
        fun showCalculations(data: CalculationsData)
        fun setAvailableTextColor(@ColorRes availableColor: Int)
        fun showAroundValue(aroundValue: BigDecimal)
        fun showButtonEnabled(isEnabled: Boolean)
        fun showSwapSuccess()
        fun openSourceSelection(tokens: List<Token>)
        fun openDestinationSelection(tokens: List<Token>)
        fun showNoPoolFound()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadInitialData()
        fun loadTokensForSourceSelection()
        fun loadTokensForDestinationSelection()
        fun setNewSourceToken(newToken: Token)
        fun setNewDestinationToken(newToken: Token)
        fun setSourceAmount(amount: BigDecimal)
        fun setSlippage(slippage: Double)
        fun swap()
    }
}
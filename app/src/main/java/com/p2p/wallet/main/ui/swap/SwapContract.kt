package com.p2p.wallet.main.ui.swap

import androidx.annotation.ColorRes
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.dashboard.model.local.Token
import java.math.BigDecimal

interface SwapContract {

    interface View : MvpView {
        fun showSourceToken(token: Token)
        fun showDestinationToken(token: Token)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showPrice(exchangeRate: BigDecimal, exchangeToken: String, perToken: String)
        fun showCalculations(data: CalculationsData)
        fun setAvailableTextColor(@ColorRes availableColor: Int)
        fun showAroundValue(aroundValue: BigDecimal)
        fun showButtonEnabled(isEnabled: Boolean)
        fun openSourceSelection(tokens: List<Token>)
        fun openDestinationSelection(tokens: List<Token>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadInitialData()
        fun loadTokensForSourceSelection()
        fun loadTokensForDestinationSelection()
        fun setNewSourceToken(newToken: Token)
        fun setNewDestinationToken(newToken: Token)
        fun setSourceAmount(amount: BigDecimal)
        fun setSlippage(slippage: Double)
    }
}
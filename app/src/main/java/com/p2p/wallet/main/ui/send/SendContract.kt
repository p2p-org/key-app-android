package com.p2p.wallet.main.ui.send

import androidx.annotation.ColorRes
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.main.model.CurrencyMode
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import com.p2p.wallet.token.model.Token
import java.math.BigDecimal

interface SendContract {

    interface View : MvpView {
        fun showSourceToken(token: Token)
        fun showSuccess(info: TransactionInfo)
        fun showWrongWalletError()
        fun navigateToTokenSelection(tokens: List<Token>)
        fun showCurrencyMode(mode: CurrencyMode)
        fun setAvailableTextColor(@ColorRes availableColor: Int)
        fun showInputValue(value: BigDecimal)
        fun showUsdAroundValue(usdValue: BigDecimal)
        fun showTokenAroundValue(tokenValue: BigDecimal, symbol: String)
        fun showAvailableValue(available: BigDecimal, symbol: String)
        fun showButtonEnabled(isEnabled: Boolean)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun send()
        fun loadInitialData()
        fun loadTokensForSelection()
        fun loadAvailableValue()
        fun setSourceToken(newToken: Token)
        fun setNewSourceAmount(amount: String)
        fun setNewTargetAddress(address: String)
        fun switchCurrency()
    }
}
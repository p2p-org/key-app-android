package com.p2p.wallet.main.ui.send

import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.main.model.CurrencyMode
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.main.ui.transaction.TransactionInfo
import java.math.BigDecimal

interface SendContract {

    interface View : MvpView {
        fun setAvailableTextColor(@AttrRes availableColor: Int)
        fun showSourceToken(token: Token)
        fun showSuccess(info: TransactionInfo)
        fun showWrongWalletError()
        fun navigateToTokenSelection(tokens: List<Token>)
        fun showCurrencyMode(mode: CurrencyMode)
        fun showButtonText(@StringRes textRes: Int)
        fun showInputValue(value: BigDecimal)
        fun showUsdAroundValue(usdValue: BigDecimal)
        fun showTokenAroundValue(tokenValue: BigDecimal, symbol: String)
        fun showAvailableValue(available: BigDecimal, symbol: String)
        fun showAddressConfirmation()
        fun hideAddressConfirmation()
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
        fun setShouldAskConfirmation(shouldAsk: Boolean)
    }
}
package org.p2p.wallet.main.ui.send

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.main.model.CurrencyMode
import org.p2p.wallet.main.model.NetworkType
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.ui.transaction.TransactionInfo
import java.math.BigDecimal

interface SendContract {

    interface View : MvpView {
        fun setAvailableTextColor(@AttrRes availableColor: Int)
        fun showSourceToken(token: Token.Active)
        fun showSuccess(info: TransactionInfo)
        fun showFee(fee: String?)
        fun showWrongWalletError()
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
        fun showNetworkDestination(type: NetworkType)
        fun showNetworkSelection()
        fun hideNetworkSelection()
        fun navigateToTokenSelection(tokens: List<Token.Active>)
        fun navigateToQrFromCamera()
        fun navigateToTypingResolve()
        fun showBufferAddressValid()
        fun showBufferAddressValidWithoutFunds()
        fun showBufferUsernameResolvedOk()
        fun showBufferUsernameResolvedNotOk()
        fun showBufferOther()
        fun setEnablePasteButton(data: String?)
    }

    interface Presenter : MvpPresenter<View> {
        fun send()
        fun loadInitialData()
        fun loadTokensForSelection()
        fun loadAvailableValue()
        fun setSourceToken(newToken: Token.Active)
        fun setNewSourceAmount(amount: String)
        fun setNewTargetAddress(address: String)
        fun switchCurrency()
        fun setShouldAskConfirmation(shouldAsk: Boolean)
        fun setNetworkDestination(networkType: NetworkType)
        fun checkClipBoard(context: Context)
    }
}
package org.p2p.wallet.main.ui.send

import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.main.model.NetworkType
import org.p2p.wallet.main.model.SearchResult
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
        fun showButtonText(@StringRes textRes: Int)
        fun showInputValue(value: BigDecimal)
        fun showUsdAroundValue(usdValue: BigDecimal)
        fun showTokenAroundValue(tokenValue: BigDecimal, symbol: String)
        fun showAvailableValue(available: BigDecimal, symbol: String)
        fun showButtonEnabled(isEnabled: Boolean)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showLoading(isLoading: Boolean)
        fun showNetworkDestination(type: NetworkType)
        fun showNetworkSelection()
        fun hideNetworkSelection()
        fun navigateToTokenSelection(tokens: List<Token.Active>)

        fun showSearchLoading(isLoading: Boolean)
        fun showIdleTarget()
        fun showWrongAddressTarget(address: String)
        fun showFullTarget(address: String, username: String)
        fun showEmptyBalanceTarget(address: String)
        fun showAddressOnlyTarget(address: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun send()
        fun loadInitialData()
        fun loadTokensForSelection()
        fun loadAvailableValue()
        fun setSourceToken(newToken: Token.Active)
        fun setTargetResult(result: SearchResult?)
        fun validateTarget(value: String)
        fun setNewSourceAmount(amount: String)
        fun switchCurrency()
        fun setNetworkDestination(networkType: NetworkType)
    }
}
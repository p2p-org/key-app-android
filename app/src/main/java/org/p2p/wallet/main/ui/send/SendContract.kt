package org.p2p.wallet.main.ui.send

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.main.model.NetworkType
import org.p2p.wallet.main.model.SearchResult
import org.p2p.wallet.main.model.SendFee
import org.p2p.wallet.main.model.SendTotal
import org.p2p.wallet.main.model.ShowProgress
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.ui.transaction.TransactionInfo
import java.math.BigDecimal

interface SendContract {

    interface View : MvpView {
        fun setAvailableTextColor(@ColorRes availableColor: Int)
        fun showSourceToken(token: Token.Active)
        fun showSuccess(info: TransactionInfo)
        fun showTotal(data: SendTotal?)
        fun showWrongWalletError()
        fun showButtonText(@StringRes textRes: Int, @DrawableRes iconRes: Int? = null, vararg value: String)
        fun showInputValue(value: BigDecimal)
        fun showUsdAroundValue(usdValue: BigDecimal)
        fun showTokenAroundValue(tokenValue: BigDecimal, symbol: String)
        fun showAvailableValue(available: BigDecimal, symbol: String)
        fun showButtonEnabled(isEnabled: Boolean)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showLoading(isLoading: Boolean)
        fun showProgressDialog(data: ShowProgress?)

        fun showNetworkDestination(type: NetworkType)
        fun showNetworkSelectionView(isVisible: Boolean)
        fun navigateToNetworkSelection(currentNetworkType: NetworkType)

        fun navigateToTokenSelection(tokens: List<Token.Active>)

        fun showSearchLoading(isLoading: Boolean)
        fun showIdleTarget()
        fun showWrongAddressTarget(address: String)
        fun showFullTarget(address: String, username: String)
        fun showEmptyBalanceTarget(address: String)
        fun showAddressOnlyTarget(address: String)
        fun showSearchScreen(usernames: List<SearchResult>)

        fun showRelayAccountFeeView(isVisible: Boolean)
        fun showAccountFeeView(fee: SendFee?)
        fun showFeePayerTokenSelector(feePayerTokens: List<Token.Active>)
    }

    interface Presenter : MvpPresenter<View> {
        fun send()
        fun loadInitialData()
        fun loadTokensForSelection()
        fun loadAvailableValue()
        fun loadCurrentNetwork()
        fun loadFeePayerTokens()
        fun setSourceToken(newToken: Token.Active)
        fun setTargetResult(result: SearchResult?)
        fun validateTarget(value: String)
        fun setNewSourceAmount(amount: String)
        fun switchCurrency()
        fun setNetworkDestination(networkType: NetworkType)
        fun setFeePayerToken(feePayerToken: Token.Active)
    }
}
package org.p2p.wallet.send.ui.main

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.send.model.NetworkType
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendConfirmData
import org.p2p.wallet.send.model.SendFee
import org.p2p.wallet.send.model.SendTotal
import org.p2p.wallet.transaction.model.ShowProgress
import java.math.BigDecimal

interface SendContract {

    interface View : MvpView {
        fun showSourceToken(token: Token.Active)
        fun showTransactionStatusMessage(amount: BigDecimal, symbol: String, isSuccess: Boolean)
        fun showTransactionDetails(transaction: HistoryTransaction)
        fun showTotal(data: SendTotal?)
        fun showDetailsError(@StringRes errorTextRes: Int?)
        fun showWrongWalletError()
        fun showButtonText(@StringRes textRes: Int, @DrawableRes iconRes: Int? = null, vararg value: String)
        fun showInputValue(value: BigDecimal, forced: Boolean)
        fun showUsdAroundValue(usdValue: BigDecimal)
        fun showTokenAroundValue(tokenValue: BigDecimal, symbol: String)
        fun showAvailableValue(available: BigDecimal, symbol: String)
        fun showButtonEnabled(isEnabled: Boolean)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showLoading(isLoading: Boolean)
        fun showProgressDialog(transactionId: String, data: ShowProgress?)
        fun setMaxButtonVisibility(isVisible: Boolean)
        fun setTotalAmountTextColor(@ColorRes textColor: Int)

        fun showNetworkDestination(type: NetworkType)
        fun showNetworkSelectionView(isVisible: Boolean)
        fun navigateToNetworkSelection(currentNetworkType: NetworkType)
        fun navigateToTokenSelection(tokens: List<Token.Active>)

        fun showAccountFeeViewLoading(isLoading: Boolean)
        fun showIndeterminateLoading(isLoading: Boolean)
        fun showIdleTarget()
        fun showWrongAddressTarget(address: String)
        fun showFullTarget(address: String, username: String)
        fun showEmptyBalanceTarget(address: String)
        fun showAddressOnlyTarget(address: String)
        fun showSearchScreen(usernames: List<SearchResult>)

        fun showWarning(@StringRes messageRes: Int?)

        fun hideAccountFeeView()
        fun showAccountFeeView(fee: SendFee)

        fun showInsufficientFundsView(tokenSymbol: String, feeUsd: BigDecimal?)

        fun showFeePayerTokenSelector(feePayerTokens: List<Token.Active>)

        fun showBiometricConfirmationPrompt(data: SendConfirmData)

        fun showScanner()
        fun showFeeLimitsDialog(maxTransactionsAvailable: Int, remaining: Int)
    }

    interface Presenter : MvpPresenter<View> {
        fun setInitialToken(initialToken: Token.Active)
        fun send()
        fun sendOrConfirm()
        fun loadInitialData()
        fun loadTokensForSelection()
        fun setMaxSourceAmountValue()
        fun loadCurrentNetwork()
        fun loadFeePayerTokens()
        fun setSourceToken(newToken: Token.Active)
        fun setTargetResult(result: SearchResult?)
        fun validateTarget(value: String)
        fun setNewSourceAmount(amount: String)
        fun switchCurrency()
        fun setNetworkDestination(networkType: NetworkType)
        fun setFeePayerToken(feePayerToken: Token.Active)
        fun onScanClicked()
        fun onFeeClicked()
    }
}

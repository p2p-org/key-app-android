package org.p2p.wallet.swap.ui.orca

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.SwapConfirmData
import org.p2p.wallet.swap.model.orca.OrcaSettingsResult
import org.p2p.wallet.swap.model.orca.SwapFee
import org.p2p.wallet.swap.model.orca.SwapPrice
import org.p2p.wallet.swap.model.orca.SwapTotal
import java.math.BigDecimal

interface OrcaSwapContract {

    interface View : MvpView {
        fun showSourceToken(token: Token.Active)
        fun showDestinationToken(token: Token?)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showLoading(isLoading: Boolean)
        fun showProgressDialog(data: ShowProgress?)
        fun showFees(data: SwapFee?)
        fun showFeePayerToken(feePayerTokenSymbol: String)
        fun showPrice(data: SwapPrice?)
        fun showTotal(data: SwapTotal?)
        fun showNewAmount(amount: String)
        fun showAroundValue(aroundValue: BigDecimal)
        fun showButtonEnabled(isEnabled: Boolean)

        fun setAvailableTextColor(@ColorRes availableColor: Int)
        fun setNewAmount(sourceAmount: String)

        fun showTransactionStatusMessage(fromSymbol: String, toSymbol: String, isSuccess: Boolean)
        fun showTransactionDetails(transaction: HistoryTransaction)
        fun showSlippage(slippage: Slippage)
        fun showSwapSettings(currentSlippage: Slippage, tokens: List<Token.Active>, currentFeePayerToken: Token.Active)
        fun showButtonText(@StringRes textRes: Int, @DrawableRes iconRes: Int? = null, vararg value: String)
        fun showSourceSelection(tokens: List<Token.Active>)
        fun showDestinationSelection(tokens: List<Token>)
        fun showError(@StringRes errorText: Int?)
        fun showFeeLimitsDialog(maxTransactionsAvailable: Int, remaining: Int)

        fun showBiometricConfirmationPrompt(data: SwapConfirmData)
        fun close()
    }

    interface Presenter : MvpPresenter<View> {
        fun loadInitialData()
        fun loadTokensForSourceSelection()
        fun loadTokensForDestinationSelection()
        fun loadDataForSettings()
        fun setNewSourceToken(newToken: Token.Active)
        fun setNewDestinationToken(newToken: Token)
        fun setSourceAmount(amount: String)
        fun setNewSettings(settingsResult: OrcaSettingsResult)
        fun swapOrConfirm()
        fun swap()
        fun calculateAvailableAmount()
        fun reverseTokens()
        fun onFeeLimitsClicked()
        fun onBackPressed()
    }
}

package org.p2p.wallet.swap.ui.orca

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.transaction.model.ShowProgress
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.send.ui.transaction.TransactionInfo
import org.p2p.wallet.swap.model.Slippage
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
        fun showPrice(data: SwapPrice?)
        fun showTotal(data: SwapTotal?)
        fun setAvailableTextColor(@AttrRes availableColor: Int)
        fun showNewAmount(amount: String)
        fun showAroundValue(aroundValue: BigDecimal)
        fun showButtonEnabled(isEnabled: Boolean)
        fun showSwapSuccess(info: TransactionInfo)
        fun showSlippage(slippage: Slippage)
        fun showButtonText(@StringRes textRes: Int, @DrawableRes iconRes: Int? = null, vararg value: String)
        fun setNewAmount(sourceAmount: String)
        fun openSourceSelection(tokens: List<Token.Active>)
        fun openDestinationSelection(tokens: List<Token>)
        fun openSwapSettings(currentSlippage: Slippage)
        fun openSwapSettings(tokens: List<Token.Active>, selectedToken: String)
        fun showError(@StringRes errorText: Int?)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadInitialData()
        fun loadTokensForSourceSelection()
        fun loadTokensForDestinationSelection()
        fun loadDataForSwapSettings()
        fun setNewSourceToken(newToken: Token.Active)
        fun setNewDestinationToken(newToken: Token)
        fun setSourceAmount(amount: String)
        fun setSlippage(slippage: Slippage)
        fun swap()
        fun calculateAvailableAmount()
        fun reverseTokens()
        fun loadTransactionTokens()
    }
}
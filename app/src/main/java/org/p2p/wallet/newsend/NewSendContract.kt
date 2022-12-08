package org.p2p.wallet.newsend

import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import java.math.BigDecimal

interface NewSendContract {
    interface View : MvpView {
        fun setSwitchLabel(symbol: String)
        fun setMainAmountLabel(symbol: String)
        fun showInputValue(value: BigDecimal, forced: Boolean)
        fun showTokenToSend(token: Token.Active)
        fun setMaxButtonVisibility(isVisible: Boolean)
        fun showAroundValue(value: String)
        fun showFeeViewLoading(isLoading: Boolean)
        fun showInsufficientFundsView(tokenSymbol: String, feeUsd: BigDecimal?)
        fun navigateToTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?)
    }

    interface Presenter : MvpPresenter<View> {
        fun setTokenToSend(newToken: Token.Active)
        fun switchCurrencyMode()
        fun setMaxAmountValue()
        fun setAmount(amount: String)
        fun onTokenClicked()
    }
}

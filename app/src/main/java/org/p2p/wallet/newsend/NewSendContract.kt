package org.p2p.wallet.newsend

import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import java.math.BigDecimal

interface NewSendContract {
    interface View : MvpView {
        fun showInputValue(value: BigDecimal, forced: Boolean)
        fun showTokenToSend(token: Token.Active)
        fun setMaxButtonVisibility(isVisible: Boolean)
        fun showAroundValue(value: BigDecimal, symbol: String)
        fun showFeeViewLoading(isLoading: Boolean)
        fun showInsufficientFundsView(tokenSymbol: String, feeUsd: BigDecimal?)
        fun navigateToTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?)
    }

    interface Presenter : MvpPresenter<View> {
        fun setTokenToSend(newToken: Token.Active)
        fun setAmount(amount: String)
        fun onTokenClicked()
    }
}

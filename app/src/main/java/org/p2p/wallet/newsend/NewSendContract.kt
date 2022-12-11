package org.p2p.wallet.newsend

import org.p2p.core.token.Token
import org.p2p.uikit.components.UiKitSendDetailsWidgetContract
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.core.common.TextContainer
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.transaction.model.ShowProgress
import java.math.BigDecimal

interface NewSendContract {
    interface View : MvpView, UiKitSendDetailsWidgetContract {
        fun updateInputValue(value: BigDecimal, forced: Boolean)

        fun showFreeTransactionsInfo()
        fun showTransactionDetails(sendFeeTotal: SendFeeTotal)
        fun showProgressDialog(internalTransactionId: String, data: ShowProgress)
        fun showInsufficientFundsView(tokenSymbol: String, feeUsd: BigDecimal?)

        fun setBottomButtonText(text: TextContainer?)
        fun setSliderText(text: String?)

        fun navigateToTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?)
    }

    interface Presenter : MvpPresenter<View> {
        fun updateToken(newToken: Token.Active)
        fun switchCurrencyMode()
        fun setMaxAmountValue()
        fun setAmount(amount: String)
        fun onTokenClicked()

        fun onFeeInfoClicked()
        fun send()
    }
}

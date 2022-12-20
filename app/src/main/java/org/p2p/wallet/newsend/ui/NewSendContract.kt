package org.p2p.wallet.newsend.ui

import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.uikit.components.UiKitSendDetailsWidgetContract
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.transaction.model.NewShowProgress

interface NewSendContract {
    interface View : MvpView, UiKitSendDetailsWidgetContract {
        fun updateInputValue(textValue: String, forced: Boolean)
        fun updateInputFraction(newInputFractionLength: Int)

        fun showFreeTransactionsInfo()
        fun showTransactionDetails(sendFeeTotal: SendFeeTotal)
        fun showAccountCreationFeeInfo(tokenSymbol: String, amountInUsd: String, hasAlternativeToken: Boolean)
        fun showProgressDialog(internalTransactionId: String, data: NewShowProgress)
        fun showTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?)
        fun showDebugInfo(text: CharSequence)

        fun setBottomButtonText(text: TextContainer?)
        fun setSliderText(text: String?)
    }

    interface Presenter : MvpPresenter<View> {
        fun setInitialToken(selectedToken: Token.Active?)

        fun updateToken(newToken: Token.Active)
        fun updateInputAmount(amount: String)
        fun updateFeePayerToken(feePayerToken: Token.Active)

        fun switchCurrencyMode()
        fun onMaxButtonClicked()

        fun onAccountCreationFeeClicked(fee: SendSolanaFee)
        fun onTokenClicked()
        fun onFeeInfoClicked()

        fun send()
    }
}

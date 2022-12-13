package org.p2p.wallet.newsend

import org.p2p.core.token.Token
import org.p2p.uikit.components.UiKitSendDetailsWidgetContract
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.core.common.TextContainer
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.transaction.model.ShowProgress

interface NewSendContract {
    interface View : MvpView, UiKitSendDetailsWidgetContract {
        fun updateInputValue(textValue: String, forced: Boolean)

        fun showFreeTransactionsInfo()
        fun showTransactionDetails(sendFeeTotal: SendFeeTotal)
        fun showAccountCreationFeeInfo(tokenSymbol: String, amountInUsd: String, hasAlternativeToken: Boolean)
        fun showProgressDialog(internalTransactionId: String, data: ShowProgress)

        fun setBottomButtonText(text: TextContainer?)
        fun setSliderText(text: String?)

        fun showTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?)
        fun showFeePayerTokenSelection(
            tokens: List<Token.Active>,
            currentFeePayerToken: Token.Active,
            approximateFeeUsd: String
        )
    }

    interface Presenter : MvpPresenter<View> {
        fun updateToken(newToken: Token.Active)
        fun updateInputAmount(amount: String)
        fun updateFeePayerToken(feePayerToken: Token.Active)

        fun switchCurrencyMode()
        fun setMaxAmountValue()

        fun onChangeFeePayerClicked(approximateFeeUsd: String)
        fun onAccountCreationFeeClicked(fee: SendSolanaFee)
        fun onTokenClicked()
        fun onFeeInfoClicked()

        fun send()
    }
}

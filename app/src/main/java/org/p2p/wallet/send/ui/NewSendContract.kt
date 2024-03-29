package org.p2p.wallet.send.ui

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.uikit.components.UiKitSendDetailsWidgetContract
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.transaction.model.NewShowProgress

interface NewSendContract {
    interface View : MvpView, UiKitSendDetailsWidgetContract {
        fun updateInputValue(textValue: String, forced: Boolean)
        fun updateInputFraction(newInputFractionLength: Int)

        fun showFreeTransactionsInfo()
        fun showTransactionDetails(
            sendFeeTotal: SendFeeTotal,
            inputAmount: BigInteger,
            useMax: Boolean,
        )
        fun showProgressDialog(internalTransactionId: String, data: NewShowProgress)
        fun showTokenSelection(selectedToken: Token.Active?)
        fun showDebugInfo(text: CharSequence)

        fun setBottomButtonText(text: TextContainer?)
        fun setSliderText(text: String?)
        fun disableSwitchAmounts()
        fun enableSwitchAmounts()
        fun disableInputs()
    }

    interface Presenter : MvpPresenter<View> {
        fun setInitialData(selectedToken: Token.Active?, inputAmount: BigDecimal?)

        fun updateToken(newToken: Token.Active)
        fun updateInputAmount(amount: String)
        fun updateFeePayerToken(feePayerToken: Token.Active)

        fun switchCurrencyMode()
        fun onMaxButtonClicked()

        fun onTokenClicked()
        fun onFeeInfoClicked()

        fun checkInternetConnection()
        fun send()
    }
}

package org.p2p.wallet.svl.ui.send

import java.math.BigInteger
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.uikit.components.UiKitSendDetailsWidgetContract
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.newsend.model.TemporaryAccount

interface SendViaLinkContract {
    interface View : MvpView, UiKitSendDetailsWidgetContract {
        fun updateInputValue(textValue: String, forced: Boolean)
        fun updateInputFraction(newInputFractionLength: Int)

        fun showFreeTransactionsInfo()
        fun showTokenSelection(tokens: List<Token.Active>, selectedToken: Token.Active?)
        fun showDebugInfo(text: CharSequence)

        fun setBottomButtonText(text: TextContainer?)
        fun setSliderText(text: String?)
        fun disableSwitchAmounts()
        fun disableInputs()

        fun navigateToLinkGeneration(
            account: TemporaryAccount,
            token: Token.Active,
            lamports: BigInteger,
            currencyModeSymbol: String
        )
        fun enableSwitchAmounts()
    }

    interface Presenter : MvpPresenter<View> {
        fun setInitialData(selectedToken: Token.Active?)

        fun updateToken(newToken: Token.Active)
        fun updateInputAmount(amount: String)

        fun switchCurrencyMode()
        fun onMaxButtonClicked()

        fun onTokenClicked()
        fun onFeeInfoClicked()

        fun checkInternetConnection()
        fun generateLink()
    }
}

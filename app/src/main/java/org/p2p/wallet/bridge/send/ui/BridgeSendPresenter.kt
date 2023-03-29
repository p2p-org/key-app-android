package org.p2p.wallet.bridge.send.ui

import android.content.res.Resources
import java.math.BigDecimal
import kotlinx.coroutines.launch
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.Token
import org.p2p.core.utils.scaleShort
import org.p2p.uikit.components.UiKitSendDetailsWidgetContract
import org.p2p.wallet.R
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.SendStateMachine
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.newsend.model.CalculationMode

class BridgeSendPresenter(
    private val stateMachine: SendStateMachine,
    private val resources: Resources,
    private val newSendAnalytics: NewSendAnalytics,
    sendModeProvider: SendModeProvider,
) : BasePresenter<BridgeSendContract.View>(), BridgeSendContract.Presenter {

    private var widgetDelegate: UiKitSendDetailsWidgetContract? = null

    private var sendToken: Token.Active? = null
    private var sendAmount: BigDecimal? = null

    private val calculationMode = CalculationMode(
        sendModeProvider = sendModeProvider,
        lessThenMinString = resources.getString(R.string.common_less_than_minimum)
    )

    override fun attach(view: BridgeSendContract.View) {
        super.attach(view)
        launch {
            stateMachine.observe().collect(::handleState)
        }
    }

    override fun attach(view: UiKitSendDetailsWidgetContract) {
        widgetDelegate = view
    }

    override fun setInitialData(token: Token.Active?, amount: BigDecimal?) {
        sendToken = token
        sendAmount = amount
    }

    override fun updateAmount(newAmount: String) {}

    override fun updateToken(newToken: Token.Active) {
        sendToken = newToken
    }

    override fun detach() {
        super.detach()
        widgetDelegate = null
    }

    override fun onFeeInfoClicked() {
        TODO("Not yet implemented")
    }

    override fun checkInternetConnection() {
        TODO("Not yet implemented")
    }

    override fun onMaxButtonClicked() {
        TODO("Not yet implemented")
    }

    override fun onTokenClicked() {
        TODO("Not yet implemented")
    }

    override fun send() {
        TODO("Not yet implemented")
    }

    override fun switchCurrencyMode() {
        TODO("Not yet implemented")
    }

    override fun updateFeePayerToken(newToken: Token.Active) {
        TODO("Not yet implemented")
    }

    private fun handleState(newState: SendState) {
        when (newState) {
            is SendState.Exception -> handleException(newState)
            is SendState.Loading -> handleLoading(newState)
            is SendState.Event -> handleStatic(newState)
        }
    }

    private fun handleException(newState: SendState.Exception) {
    }

    private fun handleStatic(newState: SendState.Event) {
        when (newState) {
            SendState.Event.SetupDefaultFields -> {
                if (calculationMode.getCurrencyMode() is CurrencyMode.Fiat) {
                    val newMode = calculationMode.switchMode()
                    newSendAnalytics.logSwitchCurrencyModeClicked(newMode)
                    widgetDelegate?.showFeeViewVisible(true)
                }
                val newTextValue = sendAmount?.scaleShort()?.toPlainString() ?: return
                view?.updateInputValue(newTextValue,forced = true)
                calculationMode.updateInputAmount(newTextValue)
                view?.disableInputs()
            }
        }
    }

    private fun handleLoading(newState: SendState.Loading) {
    }
}

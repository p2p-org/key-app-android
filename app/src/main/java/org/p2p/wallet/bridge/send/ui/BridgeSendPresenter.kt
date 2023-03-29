package org.p2p.wallet.bridge.send.ui

import java.math.BigDecimal
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.uikit.components.UiKitSendDetailsWidgetContract
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.SendStateMachine
import org.p2p.wallet.common.mvp.BasePresenter

class BridgeSendPresenter(
    private val stateMachine: SendStateMachine,
) : BasePresenter<BridgeSendContract.View>(), BridgeSendContract.Presenter {

    private var widgetDelegate: UiKitSendDetailsWidgetContract? = null

    private var sendToken: Token.Active? = null
    private var sendAmount: BigDecimal? = null

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
            }
        }
    }

    private fun handleLoading(newState: SendState.Loading) {
    }
}

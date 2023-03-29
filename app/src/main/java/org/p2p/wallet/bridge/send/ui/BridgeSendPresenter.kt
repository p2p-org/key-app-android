package org.p2p.wallet.bridge.send.ui

import android.content.res.Resources
import java.math.BigDecimal
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.model.CurrencyMode
import org.p2p.core.token.SolAddress
import org.p2p.core.token.Token
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleShort
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.uikit.components.UiKitSendDetailsWidgetContract
import org.p2p.wallet.R
import org.p2p.wallet.bridge.send.statemachine.SendFeatureAction
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.SendStateMachine
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.SearchResult

class BridgeSendPresenter(
    private val stateMachine: SendStateMachine,
    private val resources: Resources,
    private val newSendAnalytics: NewSendAnalytics,
    private val tokenKeyProvider: TokenKeyProvider,
    private val recipient: SearchResult,
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
        launch { stateMachine.observe().collect(::handleState) }
    }

    override fun attach(view: UiKitSendDetailsWidgetContract) {
        widgetDelegate = view
    }

    override fun setInitialData(token: Token.Active?, amount: BigDecimal?) {
        sendToken = token
        sendAmount = amount
        initialize()
    }

    override fun updateAmount(newAmount: String) {
        sendAmount = newAmount.toBigDecimalOrZero()
        stateMachine.newAction(SendFeatureAction.AmountChange(sendAmount.orZero()))
    }

    override fun updateToken(newToken: Token.Active) {
        sendToken = newToken
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

    override fun detach() {
        super.detach()
        stateMachine.stop()
        widgetDelegate = null
    }

    private fun initialize() {
        with(calculationMode) {
            onCalculationCompleted = { widgetDelegate?.showAroundValue(it) }
            onInputFractionUpdated = { widgetDelegate?.updateInputFraction(it) }
            onLabelsUpdated = { switchSymbol, mainSymbol ->
                widgetDelegate?.setSwitchLabel(switchSymbol)
                widgetDelegate?.setMainAmountLabel(mainSymbol)
            }
        }
        if (sendToken != null) {
            stateMachine.newAction(SendFeatureAction.RestoreSelectedToken(sendToken!!))
        } else {
            stateMachine.newAction(SendFeatureAction.SetupInitialToken(sendToken))
        }
    }

    private fun handleState(newState: SendState) {
        when (newState) {
            is SendState.Exception -> handleException(newState)
            is SendState.Loading -> handleLoading(newState)
            is SendState.Event -> handleEvent(newState)
        }
    }

    private fun handleException(newState: SendState.Exception) {
        when (newState) {
            is SendState.Exception.SnackbarMessage -> {
                view?.showUiKitSnackBar(messageResId = newState.messageResId)
            }
            is SendState.Exception.FeeLoading -> {
                stateMachine.startRecurringAction(buildRefreshFeeAction())
            }
        }
    }

    private fun handleEvent(state: SendState.Event) {
        when (state) {
            is SendState.Event.SetupDefaultFields -> {
                onSetupDefaultState(state)
                stateMachine.startRecurringAction(buildRefreshFeeAction())
            }
            is SendState.Event.UpdateFee -> {}
        }
    }

    private fun handleLoading(newState: SendState.Loading) {
        when (newState) {
            is SendState.Loading.Fee -> {
                widgetDelegate?.showFeeViewLoading(true)
            }
        }
    }

    private fun setSendToken(newToken: Token.Active?) {
        if (newToken != null) {
            widgetDelegate?.showToken(newToken)
            calculationMode.updateToken(newToken)
        }
    }

    private fun onSetupDefaultState(state: SendState.Event.SetupDefaultFields) {
        calculationMode.updateToken(state.initToken)

        if (calculationMode.getCurrencyMode() is CurrencyMode.Fiat) {
            val newMode = calculationMode.switchMode()
            newSendAnalytics.logSwitchCurrencyModeClicked(newMode)
            widgetDelegate?.showFeeViewVisible(true)
        }

        val defaultAmount = sendAmount.orZero()
        val newTextValue = defaultAmount.scaleShort().toPlainString()

        view?.updateInputValue(newTextValue, forced = true)
        calculationMode.updateInputAmount(newTextValue)
        setSendToken(state.initToken)
        widgetDelegate?.setTokenContainerEnabled(isEnabled = state.isTokenChangeEnabled)
        widgetDelegate?.setFeeLabel(resources.getString(R.string.send_fees))
        view?.setBottomButtonText(TextContainer.Res(R.string.send_calculating_fees))
        view?.disableInputs()
    }

    private fun onUpdateFeeState(state: SendState.Event.UpdateFee) {
        val fees = state.fee
    }

    private fun buildRefreshFeeAction(): SendFeatureAction {
        return SendFeatureAction.RefreshFee(
            userWallet = SolAddress(tokenKeyProvider.publicKey),
            recipient = EthAddress(recipient.addressState.address),
            mintAddress = sendToken?.mintAddress?.let { SolAddress(it) },
            amount = sendAmount.orZero().toPlainString()
        )
    }
}

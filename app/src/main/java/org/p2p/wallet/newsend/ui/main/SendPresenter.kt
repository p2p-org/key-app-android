package org.p2p.wallet.newsend.ui.main

import android.content.res.Resources
import timber.log.Timber
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.newsend.model.CalculationEvent
import org.p2p.wallet.newsend.model.FeeLoadingState
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.SendActionEvent
import org.p2p.wallet.newsend.model.SendSolanaFee
import org.p2p.wallet.newsend.model.SendState
import org.p2p.wallet.newsend.model.main.WidgetState
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason
import org.p2p.wallet.newsend.smartselection.FeeDebugInfoBuilder
import org.p2p.wallet.newsend.smartselection.SendButtonMapper
import org.p2p.wallet.newsend.smartselection.SendStateManager
import org.p2p.wallet.updates.NetworkConnectionStateProvider

private const val TAG = "SendPresenter"

class SendPresenter(
    private val resources: Resources,
    private val connectionStateProvider: NetworkConnectionStateProvider,
    private val feeDebugInfoBuilder: FeeDebugInfoBuilder,
    private val newSendAnalytics: NewSendAnalytics,
    private val sendStateManager: SendStateManager,
    private val sendButtonMapper: SendButtonMapper
) : BasePresenter<SendContract.View>(), SendContract.Presenter {

    override fun attach(view: SendContract.View) {
        super.attach(view)
        logScreenOpened()

        observeState()

        sendStateManager.onNewEvent(SendActionEvent.InitialLoading)
    }

    override fun detach() {
        sendStateManager.release()
        super.detach()
    }

    private fun observeState() {
        launch {
            sendStateManager.observeState()
                .consumeAsFlow()
                .collect { handleState(it) }
        }
    }

    private fun handleState(newState: SendState) {
        Timber.tag(TAG).i("Received SendCommonState: $newState")

        when (newState) {
            is SendState.Idle -> Unit
            is SendState.FeePayerUpdate -> handleFeePayerState(newState.feePayerUpdate)
            is SendState.CalculationUpdate -> handleCalculationEvent(newState.calculationEvent)
            is SendState.WidgetUpdate -> handleWidgetState(newState.widgetState)
            is SendState.ShowFreeTransactionDetails -> handleFreeTransactionClicked()
            is SendState.ShowTransactionDetails -> view?.showTransactionDetails(newState.feeTotal)
            is SendState.ShowTokenSelection -> view?.showTokenSelection(newState.currentToken)
            is SendState.Loading -> handleLoadingState(newState.loadingState)
            is SendState.ShowProgress -> view?.showProgressDialog(newState.internalTransactionUuid, newState.data)
            is SendState.GeneralError -> handleGeneralError(newState.cause)
        }
    }

    private fun handleCalculationEvent(newEvent: CalculationEvent) {
        Timber.tag(TAG).i("Received CalculationState: $newEvent")

        when (newEvent) {
            is CalculationEvent.TokenUpdated -> handleTokenUpdated(newEvent)
            is CalculationEvent.AmountChanged -> handleAmountChanged(newEvent)
            is CalculationEvent.AmountReduced -> handleAmountReduced(newEvent)
            is CalculationEvent.MaxValueEntered -> handleMaxInputEntered(newEvent)
            is CalculationEvent.CurrencySwitched -> handleCurrencySwitched(newEvent)
            is CalculationEvent.Idle -> Unit
        }
    }

    private fun handleFeePayerState(newState: FeePayerState) {
        Timber.tag(TAG).i("Received FeePayerState: $newState")

        when (newState) {
            is FeePayerState.Idle -> Unit
            is FeePayerState.FreeTransaction -> handleFreeTransaction(newState)
            is FeePayerState.CalculationSuccess -> handleCalculationSuccess(newState)
            is FeePayerState.ReduceAmount -> handleReduceAmount(newState)
            is FeePayerState.NoStrategiesFound -> view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            is FeePayerState.Failure -> handleFailure(newState.reason)
        }

        updateButtonState(newState)
    }

    private fun handleCurrencySwitched(newState: CalculationEvent.CurrencySwitched) {
        view?.updateInputFraction(newState.fraction)

        view?.updateInputValue(newState.newInputAmount)
        view?.showApproximateAmount(newState.approximateAmount)
        view?.setSwitchLabel(newState.switchInputSymbol)
        view?.setMainAmountLabel(newState.currentInputSymbol)

        newSendAnalytics.logSwitchCurrencyModeClicked(isCryptoMode = newState.isFiat)
    }

    private fun handleAmountReduced(newState: CalculationEvent.AmountReduced) {
        view?.showApproximateAmount(newState.approximateAmount)
        view?.setMaxButtonVisible(isVisible = newState.isMaxButtonVisible)
        view?.updateInputValue(newState.newInputAmount)
    }

    private fun handleAmountChanged(newState: CalculationEvent.AmountChanged) {
        view?.showApproximateAmount(newState.approximateAmount)
        view?.setMaxButtonVisible(isVisible = newState.isMaxButtonVisible)
    }

    private fun handleTokenUpdated(newState: CalculationEvent.TokenUpdated) {
        view?.setSwitchLabel(newState.switchInputSymbol)
        view?.setMainAmountLabel(newState.currentInputSymbol)
        view?.updateInputFraction(newState.fraction)
        view?.showApproximateAmount(newState.approximateAmount)
    }

    private fun handleFailure(reason: FeePayerFailureReason) {
        when (reason) {
            is FeePayerFailureReason.CalculationError -> {
                view?.showFeeVisible(isVisible = false)
            }
            is FeePayerFailureReason.InputExceeded -> Unit
            is FeePayerFailureReason.ExceededFee -> Unit
            is FeePayerFailureReason.LowMinBalanceIgnored -> Unit
            is FeePayerFailureReason.InvalidAmountForRecipient -> Unit
            is FeePayerFailureReason.InvalidAmountForSender -> Unit
        }
    }

    private fun handleWidgetState(newState: WidgetState) {
        when (newState) {
            is WidgetState.DisableInput -> handleDisableInput(newState)
            is WidgetState.EnableCurrencySwitch -> handleCurrencySwitch(newState)
            is WidgetState.TokenSelectionEnabled -> view?.setTokenContainerEnabled(isEnabled = newState.isEnabled)
            is WidgetState.TokenUpdated -> view?.showToken(newState.updatedToken)
            is WidgetState.InputUpdated -> view?.updateInputValue(newState.newInputValue)
        }
    }

    private fun handleLoadingState(loadingState: FeeLoadingState) {
        if (loadingState.isFeeLoading()) {
            view?.showFeeVisible(isVisible = true)
            view?.setFeeLabel(resources.getString(R.string.send_fees))
            view?.setBottomButtonText(TextContainer.Res(R.string.send_calculating_fees))
        }

        when (loadingState) {
            is FeeLoadingState.Instant -> view?.showFeeViewLoading(isLoading = loadingState.isLoading)
            is FeeLoadingState.Delayed -> view?.showDelayedFeeViewLoading(isLoading = loadingState.isLoading)
        }
    }

    private fun handleGeneralError(cause: Throwable) {
        view?.showErrorMessage(cause)
    }

    private fun handleFreeTransaction(newState: FeePayerState.FreeTransaction) {
        view?.showFeeViewLoading(isLoading = false)

        if (newState.initialAmount == null) {
            view?.setFeeLabel(resources.getString(R.string.send_fees_free))
            return
        }

        val label = "${resources.getString(R.string.send_fees_zero)} ${newState.sourceToken.tokenSymbol}"
        view?.setFeeLabel(label)
    }

    private fun handleCalculationSuccess(newState: FeePayerState.CalculationSuccess) {
        val solanaFee = SendSolanaFee(
            feePayerToken = newState.feePayerToken,
            feeRelayerFee = newState.fee,
            sourceToken = newState.sourceToken
        )

        if (newState.inputAmount == null) {
            view?.setFeeLabel(resources.getString(R.string.send_fees_free))
            return
        }

        view?.setFeeLabel(resources.getString(R.string.send_fees_format, solanaFee.totalFee))

        if (BuildConfig.DEBUG) showFeeDebugText(solanaFee)
    }

    private fun handleReduceAmount(newState: FeePayerState.ReduceAmount) {
        sendStateManager.onNewEvent(SendActionEvent.ReduceAmount(newState.newInputAmount))
        view?.showUiKitSnackBar(resources.getString(R.string.send_reduced_amount_calculation_message))
    }

    private fun handleDisableInput(newState: WidgetState.DisableInput) {
        view?.updateInputValue(newState.inputAmount)
        view?.disableInputs()
    }

    private fun handleCurrencySwitch(newState: WidgetState.EnableCurrencySwitch) {
        if (newState.isEnabled) {
            view?.enableSwitchAmounts()
        } else {
            view?.disableSwitchAmounts()
        }
    }

    private fun handleFreeTransactionClicked() {
        newSendAnalytics.logFreeTransactionsClicked()
        view?.showFreeTransactionsInfo()
    }

    private fun handleMaxInputEntered(newState: CalculationEvent.MaxValueEntered) {
        view?.setMaxButtonVisible(isVisible = newState.isMaxButtonVisible)
        view?.updateInputValue(textValue = newState.newInputAmount)
        view?.showApproximateAmount(approximateAmount = newState.approximateAmount)
        val message = resources.getString(R.string.send_using_max_amount, newState.sourceTokenSymbol)
        view?.showToast(TextContainer.Raw(message))
    }

    override fun onTokenClicked() {
        newSendAnalytics.logTokenSelectionClicked()
        sendStateManager.onNewEvent(SendActionEvent.OnTokenClicked)
    }

    override fun updateToken(newToken: Token.Active) {
        sendStateManager.onNewEvent(SendActionEvent.SourceTokenChanged(newToken))
    }

    override fun switchCurrencyMode() {
        sendStateManager.onNewEvent(SendActionEvent.CurrencyModeSwitched)
    }

    override fun updateInputAmount(amount: String) {
        newSendAnalytics.setMaxButtonClicked(isClicked = false)
        sendStateManager.onNewEvent(SendActionEvent.AmountChanged(amount))
    }

    override fun updateFeePayerToken(feePayerToken: Token.Active) {
        sendStateManager.onNewEvent(SendActionEvent.FeePayerManuallyChanged(feePayerToken))
    }

    override fun onMaxButtonClicked() {
        newSendAnalytics.setMaxButtonClicked(isClicked = true)
        sendStateManager.onNewEvent(SendActionEvent.MaxAmountEntered)
    }

    override fun onFeeInfoClicked() {
        sendStateManager.onNewEvent(SendActionEvent.OnFeeClicked)
    }

    override fun checkInternetConnection() {
        if (!connectionStateProvider.hasConnection()) {
            view?.showUiKitSnackBar(
                message = resources.getString(R.string.error_no_internet_message),
                actionButtonResId = R.string.common_hide
            )
            view?.restoreSlider()
            return
        }

        view?.showSliderCompleteAnimation()
    }

    override fun send() {
        sendStateManager.onNewEvent(SendActionEvent.LaunchSending)
    }

    private fun updateButtonState(newState: FeePayerState) {
        when (val state = sendButtonMapper.mapToButtonState(newState)) {
            is SendButtonMapper.State.Disabled -> {
                view?.setBottomButtonText(state.textContainer)
                view?.setSliderText(null)
                view?.setInputColor(state.totalAmountTextColor)
            }

            is SendButtonMapper.State.Enabled -> {
                view?.setSliderText(resources.getString(state.textResId, state.value))
                view?.setBottomButtonText(null)
                view?.setInputColor(state.totalAmountTextColor)
            }
        }
    }

    private fun showFeeDebugText(solanaFee: SendSolanaFee?) {
        launch {
            val debugInfo = feeDebugInfoBuilder.buildDebugInfo(solanaFee)
            view?.showDebugInfo(debugInfo)
        }
    }

    private fun logScreenOpened() {
        newSendAnalytics.logNewSendScreenOpened()
    }
}

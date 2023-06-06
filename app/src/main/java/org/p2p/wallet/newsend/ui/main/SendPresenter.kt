package org.p2p.wallet.newsend.ui.main

import android.content.res.Resources
import org.threeten.bp.ZonedDateTime
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.common.TextContainer
import org.p2p.core.token.Token
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.infrastructure.network.provider.SendModeProvider
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.CalculationState
import org.p2p.wallet.newsend.model.FeeLoadingState
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.FeesStringFormat
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.model.SendActionEvent
import org.p2p.wallet.newsend.model.SendSolanaFee
import org.p2p.wallet.newsend.model.SendState
import org.p2p.wallet.newsend.model.main.WidgetState
import org.p2p.wallet.newsend.model.smartselection.FeePayerFailureReason
import org.p2p.wallet.newsend.smartselection.FeeDebugInfoBuilder
import org.p2p.wallet.newsend.smartselection.SendStateManager
import org.p2p.wallet.newsend.smartselection.initial.SendInitialData
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.updates.NetworkConnectionStateProvider
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle

private const val TAG = "NewSendPresenter"

class SendPresenter(
    private val initialData: SendInitialData,
    private val userInteractor: UserInteractor,
    private val resources: Resources,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionManager: TransactionManager,
    private val connectionStateProvider: NetworkConnectionStateProvider,
    private val feeDebugInfoBuilder: FeeDebugInfoBuilder,
    private val newSendAnalytics: NewSendAnalytics,
    private val alertErrorsLogger: AlarmErrorsLogger,
    private val appScope: AppScope,
    private val historyInteractor: HistoryInteractor,
    private val sendStateManager: SendStateManager,
    private val sendButtonStateManager: SendButtonStateManager,
    sendModeProvider: SendModeProvider
) : BasePresenter<SendContract.View>(), SendContract.Presenter {

    @Deprecated("will be moved to [SendStateManager]")
    private val calculationMode = CalculationMode(
        sendModeProvider = sendModeProvider, lessThenMinString = resources.getString(R.string.common_less_than_minimum)
    )

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
        sendStateManager.getStateFlow().onEach(::handleState).launchIn(this)
    }

    private fun handleState(newState: SendState) {
        when (newState) {
            is SendState.Idle -> Unit
            is SendState.CalculationUpdate -> handleCalculationState(newState.calculationState)
            is SendState.FeePayerUpdate -> handleFeePayerState(newState.feePayerState)
            is SendState.WidgetUpdate -> handleWidgetState(newState.widgetState)
            is SendState.ShowFreeTransactionDetails -> handleFreeTransactionClicked()
            is SendState.ShowTransactionDetails -> view?.showTransactionDetails(newState.feeTotal)
            is SendState.ShowTokenSelection -> view?.showTokenSelection(newState.currentToken)
            is SendState.Loading -> handleLoadingState(newState.loadingState)
            is SendState.GeneralError -> handleGeneralError(newState.cause)
        }
    }

    private fun handleCalculationState(newState: CalculationState) {
        when (newState) {
            is CalculationState.CalculationCompleted -> {
                view?.showAroundValue(newState.aroundValue)
            }
            is CalculationState.InputFractionUpdate -> {
                view?.updateInputFraction(newState.fraction)
            }
            is CalculationState.LabelsUpdate -> {
                view?.setSwitchLabel(newState.switchSymbol)
                view?.setMainAmountLabel(newState.mainSymbol)
            }
            is CalculationState.MaxValueEntered -> {
                handleMaxInputEntered(newState)
            }
            is CalculationState.MaxButtonVisible -> {
                view?.setMaxButtonVisible(isVisible = newState.isMaxButtonVisible)
            }
            is CalculationState.CurrencySwitched -> {
                view?.setSwitchLabel(newState.switchSymbol)
                view?.setMainAmountLabel(newState.mainSymbol)
                view?.updateInputValue(newState.newInputAmount)

                newSendAnalytics.logSwitchCurrencyModeClicked(isCryptoMode = newState.isFiat)
            }
            is CalculationState.Idle -> Unit
        }
    }

    private fun handleFeePayerState(newState: FeePayerState) {
        when (newState) {
            is FeePayerState.Idle -> Unit
            is FeePayerState.FreeTransaction -> handleFreeTransaction(newState)
            is FeePayerState.CalculationSuccess -> handleCalculationSuccess(newState)
            is FeePayerState.ReduceAmount -> handleReduceAmount(newState)
            is FeePayerState.NoStrategiesFound -> view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            is FeePayerState.Failure -> handleFailure(newState.reason)
        }

        validateButtonState(newState)
    }

    private fun handleFailure(reason: FeePayerFailureReason) {
        when (reason) {
            is FeePayerFailureReason.CalculationError -> Unit
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
            is WidgetState.InputUpdated -> view?.updateInputValue(newState.newInput)
        }
    }

    private fun handleLoadingState(loadingState: FeeLoadingState) {
        if (loadingState.isLoading()) {
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
        val textRes = if (newState.initialAmount == null) R.string.send_fees_free else R.string.send_fees_zero
        view?.setFeeLabel(FeesStringFormat(textRes).format(resources))
    }

    private fun handleCalculationSuccess(newState: FeePayerState.CalculationSuccess) {
        val solanaFee = SendSolanaFee(
            feePayerToken = newState.feePayerToken,
            feeRelayerFee = newState.fee,
            sourceToken = newState.sourceToken
        )

        val feesFormat = FeesStringFormat(R.string.send_fees_format, solanaFee.totalFee)
        view?.setFeeLabel(feesFormat.format(resources))

        if (BuildConfig.DEBUG) showFeeDebugText(solanaFee)
    }

    private fun handleReduceAmount(newState: FeePayerState.ReduceAmount) {
        view?.showUiKitSnackBar(resources.getString(R.string.send_reduced_amount_calculation_message))
        view?.updateInputValue(newState.newInputAmount.toPlainString())
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

    private fun handleMaxInputEntered(newState: CalculationState.MaxValueEntered) {
        view?.setMaxButtonVisible(isVisible = newState.isMaxButtonVisible)
        view?.updateInputValue(textValue = newState.newInputAmount)

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
//        val token = requireToken()
//
//        val address = initialData.recipient.addressState.address
//        val currentAmount = calculationMode.getCurrentAmount()
//        val currentAmountUsd = calculationMode.getCurrentAmountUsd()
//        val lamports = calculationMode.getCurrentAmountLamports()
//
//        logSendClicked(token, currentAmount.toPlainString(), currentAmountUsd.orZero().toPlainString())
//
//        // the internal id for controlling the transaction state
//        val internalTransactionId = UUID.randomUUID().toString()
//
//        val total = sendStateManager.buildTotalFee(
//            sourceToken = requireToken(), calculationMode = calculationMode
//        )
//
//        appScope.launch {
//            val transactionDate = ZonedDateTime.now()
//            try {
//                val progressDetails = NewShowProgress(date = transactionDate,
//                    tokenUrl = token.iconUrl.orEmpty(),
//                    amountTokens = "${currentAmount.toPlainString()} ${token.tokenSymbol}",
//                    amountUsd = currentAmountUsd?.asNegativeUsdTransaction(),
//                    recipient = initialData.recipient.nicknameOrAddress(),
//                    totalFees = total.getFeesCombined(checkFeePayer = false)?.let { listOf(it) })
//                view?.showProgressDialog(internalTransactionId, progressDetails)
//
//                val result = sendStateManager.sendTransaction(address.toPublicKey(), token, lamports)
//                userInteractor.addRecipient(initialData.recipient, Date(transactionDate.dateMilli()))
//                val transaction = buildTransaction(result, token)
//                val transactionState = TransactionState.SendSuccess(transaction, token.tokenSymbol)
//                transactionManager.emitTransactionState(internalTransactionId, transactionState)
//                historyInteractor.addPendingTransaction(
//                    txSignature = result, transaction = transaction, mintAddress = token.mintAddress.toBase58Instance()
//                )
//            } catch (e: Throwable) {
//                Timber.tag(TAG).e(e, "Failed sending transaction!")
//                val message = e.getErrorMessage { res -> resources.getString(res) }
//                transactionManager.emitTransactionState(internalTransactionId, TransactionState.Error(message))
//                logSendError(token, e)
//            }
//        }
    }

    private fun SearchResult.nicknameOrAddress(): String {
        return if (this is SearchResult.UsernameFound) formattedUsername
        else addressState.address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)
    }

    private fun buildTransaction(transactionId: String, token: Token.Active): HistoryTransaction =
        RpcHistoryTransaction.Transfer(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = -1,
            type = RpcHistoryTransactionType.SEND,
            senderAddress = tokenKeyProvider.publicKey,
            amount = RpcHistoryAmount(calculationMode.getCurrentAmount(), calculationMode.getCurrentAmountUsd()),
            destination = initialData.recipient.addressState.address,
            counterPartyUsername = initialData.recipient.nicknameOrAddress(),
            fees = null,
            status = HistoryTransactionStatus.PENDING,
            iconUrl = token.iconUrl,
            symbol = token.tokenSymbol
        )

    private fun validateButtonState(newState: FeePayerState) {
        when (val state = sendButtonStateManager.validate(newState)) {
            is SendButtonStateManager.State.Disabled -> {
                view?.setBottomButtonText(state.textContainer)
                view?.setSliderText(null)
                view?.setInputColor(state.totalAmountTextColor)
            }

            is SendButtonStateManager.State.Enabled -> {
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

    private fun logSendClicked(token: Token.Active, amountInToken: String, amountInUsd: String) {
//        val solanaFee = (sendStateManager.getState() as? SendState.UpdateFee)?.solanaFee
//        newSendAnalytics.logSendConfirmButtonClicked(
//            tokenName = token.tokenName,
//            amountInToken = amountInToken,
//            amountInUsd = amountInUsd,
//            isFeeFree = solanaFee?.isTransactionFree ?: false,
//            mode = calculationMode.getCurrencyMode()
//        )
    }

    private fun logSendError(
        token: Token.Active?, error: Throwable
    ) {
//        if (token == null) return
//
//        launch {
//            val fee = sendStateManager.getState().getFee()
//            val accountCreationFee = fee?.accountCreationFeeDecimals?.toPlainString()
//            val transactionFee = fee?.transactionDecimals?.toPlainString()
//            alertErrorsLogger.triggerSendAlarm(
//                token = token,
//                currencyMode = calculationMode.getCurrencyMode(),
//                amount = calculationMode.getCurrentAmount().toPlainString(),
//                feePayerToken = sendStateManager.getFeePayerToken(),
//                accountCreationFee = accountCreationFee,
//                transactionFee = transactionFee,
//                relayAccount = sendStateManager.getUserRelayAccount(),
//                recipientAddress = initialData.recipient,
//                error = error
//            )
//        }
    }

    private fun logScreenOpened() {
        newSendAnalytics.logNewSendScreenOpened()
    }
}

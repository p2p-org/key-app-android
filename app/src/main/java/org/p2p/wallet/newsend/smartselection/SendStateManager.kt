package org.p2p.wallet.newsend.smartselection

import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.util.Date
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.token.findByMintAddress
import org.p2p.core.utils.asNegativeUsdTransaction
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toUsd
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.common.date.dateMilli
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryAmount
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransactionType
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.newsend.analytics.NewSendAnalytics
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.FeeLoadingState
import org.p2p.wallet.newsend.model.SendActionEvent
import org.p2p.wallet.newsend.model.SendFeeTotal
import org.p2p.wallet.newsend.model.SendSolanaFee
import org.p2p.wallet.newsend.model.SendState
import org.p2p.wallet.newsend.model.SendState.GeneralError
import org.p2p.wallet.newsend.model.SendState.Loading
import org.p2p.wallet.newsend.model.main.WidgetState
import org.p2p.wallet.newsend.model.nicknameOrAddress
import org.p2p.wallet.newsend.smartselection.initial.SendInitialData
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.model.TransactionState
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.toBase58Instance
import org.p2p.wallet.utils.toPublicKey

private const val TAG = "SendFeeRelayerManager"

class SendStateManager(
    private val initialData: SendInitialData,
    private val sendInteractor: SendInteractor,
    private val userInteractor: UserInteractor,
    private val smartSelectionCoordinator: SmartSelectionCoordinator,
    private val inputCalculator: SendInputCalculator,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionManager: TransactionManager,
    private val historyInteractor: HistoryInteractor,
    private val newSendAnalytics: NewSendAnalytics,
    private val alertErrorsLogger: AlarmErrorsLogger,
    private val appScope: AppScope,
    dispatchers: CoroutineDispatchers
) : CoroutineScope {

    override val coroutineContext: CoroutineContext =
        SupervisorJob() + dispatchers.io + Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val currentState: Channel<SendState> = Channel()

    private lateinit var feeLimitInfo: TransactionFeeLimits
    private lateinit var solToken: Token.Active
    private lateinit var sourceToken: Token.Active

    private var observeTokensJob: Job? = null

    init {
        smartSelectionCoordinator.getFeePayerStateFlow()
            .onEach {
                val feePayerUpdate = SendState.FeePayerUpdate(it)
                updateState(feePayerUpdate)
            }
            .launchIn(this)

        inputCalculator.getCalculationStateFlow()
            .onEach {
                val calculationUpdate = SendState.CalculationUpdate(it)
                updateState(calculationUpdate)
            }
            .launchIn(this)
    }

    fun observeState(): Channel<SendState> = currentState

    fun onNewEvent(newEvent: SendActionEvent) {
        when (newEvent) {
            is SendActionEvent.InitialLoading -> executeInitialLoading()
            is SendActionEvent.AmountChanged -> handleAmountChanged(newEvent)
            is SendActionEvent.SourceTokenChanged -> handleSourceTokenChanged(newEvent)
            is SendActionEvent.FeePayerManuallyChanged -> handleFeePayerChanged(newEvent)
            is SendActionEvent.ReduceAmount -> inputCalculator.reduceAmount(newEvent.newInputAmount)
            is SendActionEvent.MaxAmountEntered -> handleMaxAmountEntered()
            is SendActionEvent.CurrencyModeSwitched -> handleCurrencyModeSwitched()
            is SendActionEvent.OnFeeClicked -> handleFeeClicked()
            is SendActionEvent.OnTokenClicked -> handleTokenClicked()
            is SendActionEvent.LaunchSending -> handleLaunchSending()
        }
    }

    private fun handleLaunchSending() {
        val address = initialData.recipient.address
        val currentAmount = inputCalculator.getCurrentAmount()
        val currentAmountUsd = inputCalculator.getCurrentAmountUsd()
        val lamports = inputCalculator.getCurrentAmountLamports()
        val feePayerToken = try {
            smartSelectionCoordinator.requireFeePayer()
        } catch (e: Throwable) {
            updateState(GeneralError(e))
            return
        }

        // the internal id for controlling the transaction state
        val internalTransactionId = UUID.randomUUID().toString()
        val total = createFeeTotal()

        logSendClicked(total.sendFee, currentAmount.toPlainString(), currentAmountUsd.orZero().toPlainString())

        val transactionDate = ZonedDateTime.now()

        val progressDetails = NewShowProgress(
            date = transactionDate,
            tokenUrl = sourceToken.iconUrl.orEmpty(),
            amountTokens = "${currentAmount.toPlainString()} ${sourceToken.tokenSymbol}",
            amountUsd = currentAmountUsd?.asNegativeUsdTransaction(),
            recipient = initialData.recipient.nicknameOrAddress(),
            totalFees = total.getFeesCombined(checkFeePayer = false)?.let { listOf(it) }
        )

        updateState(SendState.ShowProgress(internalTransactionId, progressDetails))

        appScope.launch {
            try {
                val result = sendInteractor.sendTransaction(address.toPublicKey(), sourceToken, feePayerToken, lamports)
                userInteractor.addRecipient(initialData.recipient, Date(transactionDate.dateMilli()))

                val transaction = buildTransaction(result)
                val transactionState = TransactionState.SendSuccess(transaction, sourceToken.tokenSymbol)
                transactionManager.emitTransactionState(internalTransactionId, transactionState)
                historyInteractor.addPendingTransaction(
                    txSignature = result,
                    transaction = transaction,
                    mintAddress = sourceToken.mintAddress.toBase58Instance()
                )
            } catch (e: Throwable) {
                Timber.tag(TAG).e(e, "Failed sending transaction!")
                transactionManager.emitTransactionState(internalTransactionId, TransactionState.Error(e))
                logSendError(e, feePayerToken, total.sendFee)
            }
        }
    }

    // Load or restore the initial send state
    private fun executeInitialLoading() {
        launch {
            setLoading(isLoading = true)

            val userTokens = userInteractor.getNonZeroUserTokens()
            initializeScreen(userTokens)

            val trigger = SmartSelectionTrigger.Initialization(
                initialToken = sourceToken,
                initialAmount = initialData.inputAmount
            )
            smartSelectionCoordinator.updateFeePayer(sourceToken)
            smartSelectionCoordinator.onNewTrigger(trigger)

            validateCurrencySwitch()
            validateTokenSelection(userTokens)
            validateInput()

            observeTokenUpdates(sourceToken)

            setLoading(isLoading = false)
        }
    }

    private suspend fun initializeScreen(userTokens: List<Token.Active>) {
        try {
            sourceToken = initialData.selectInitialToken(userTokens)
            solToken = initialData.findSolToken(sourceToken, userInteractor.getUserSolToken())

            feeLimitInfo = sendInteractor.getFreeTransactionsInfo(useCache = false)
            sendInteractor.getMinRelayRentExemption().also { inputCalculator.saveMinRentExemption(it) }
            sendInteractor.initialize()

            updateToken(sourceToken)
        } catch (e: Throwable) {
            Timber.tag(TAG).i(e, "Send initial loading failed")
            updateState(GeneralError(e))
        }
    }

    private fun handleAmountChanged(event: SendActionEvent.AmountChanged) {
        inputCalculator.updateInputAmount(event.newInputAmount)

        val trigger = SmartSelectionTrigger.AmountChanged(
            solToken = solToken,
            sourceToken = sourceToken,
            inputAmount = inputCalculator.getCurrentAmount().takeIf { it.isNotZero() },

        )
        smartSelectionCoordinator.onNewTrigger(trigger)
    }

    private fun handleSourceTokenChanged(event: SendActionEvent.SourceTokenChanged) {
        updateToken(updatedToken = event.newSourceToken)

        validateCurrencySwitch()

        val trigger = SmartSelectionTrigger.SourceTokenChanged(
            solToken = solToken,
            newSourceToken = event.newSourceToken,
            inputAmount = inputCalculator.getCurrentAmount().takeIf { it.isNotZero() }
        )
        smartSelectionCoordinator.onNewTrigger(trigger)
    }

    private fun handleFeePayerChanged(event: SendActionEvent.FeePayerManuallyChanged) {
        val trigger = SmartSelectionTrigger.FeePayerManuallyChanged(
            sourceToken = sourceToken,
            newFeePayer = event.newFeePayerToken,
            inputAmount = inputCalculator.getCurrentAmount()
        )
        smartSelectionCoordinator.onNewTrigger(trigger)
    }

    private fun handleMaxAmountEntered() {
        // updates the current amount value inside
        inputCalculator.onMaxClicked()
        val maxAmount = inputCalculator.getCurrentAmount()
        val trigger = SmartSelectionTrigger.MaxAmountEntered(
            solToken = solToken,
            sourceToken = sourceToken,
            inputAmount = maxAmount
        )
        smartSelectionCoordinator.onNewTrigger(trigger)
    }

    private fun handleCurrencyModeSwitched() {
        inputCalculator.toggleMode()
    }

    private fun handleTokenClicked() {
        updateState(SendState.ShowTokenSelection(sourceToken))
    }

    private fun handleFeeClicked() {
        if (smartSelectionCoordinator.isFreeAndInputEmpty()) {
            updateState(SendState.ShowFreeTransactionDetails)
        } else {
            updateState(SendState.ShowTransactionDetails(createFeeTotal()))
        }
    }

    private fun observeTokenUpdates(token: Token.Active) {
        observeTokensJob?.cancel()
        userInteractor.getUserTokensFlow()
            .map { it.findByMintAddress(token.mintAddress) }
            .filterNotNull()
            .onEach { updateToken(updatedToken = it) }
            .launchIn(this)
            .also { observeTokensJob = it }
    }

    private fun validateCurrencySwitch() {
        val isCurrencyDisabled = sourceToken.isCurrencyDisabled()
        val widgetState = WidgetState.EnableCurrencySwitch(isEnabled = !isCurrencyDisabled)
        val newState = SendState.WidgetUpdate(widgetState)
        updateState(newState)

        if (isCurrencyDisabled) {
            inputCalculator.enableTokenMode()
        }
    }

    private fun validateInput() {
        if (initialData.isInitialAmountEntered()) {
            val inputAmount = initialData.inputAmount!!.toPlainString()
            val widgetState = WidgetState.DisableInput(inputAmount = inputAmount)
            val newState = SendState.WidgetUpdate(widgetState)
            updateState(newState)

            inputCalculator.enableTokenMode()
            inputCalculator.updateInputAmount(inputAmount)
        }
    }

    private fun validateTokenSelection(userTokens: List<Token.Active>) {
        val isEnabled = initialData.isTokenSelectionEnabled(userTokens)

        val widgetState = WidgetState.TokenSelectionEnabled(isEnabled = isEnabled)
        val newState = SendState.WidgetUpdate(widgetState)
        updateState(newState)
    }

    private fun updateToken(updatedToken: Token.Active) {
        sourceToken = updatedToken
        inputCalculator.updateToken(newToken = updatedToken)

        val widgetState = WidgetState.TokenUpdated(updatedToken)
        updateState(SendState.WidgetUpdate(widgetState))
    }

    fun release() {
        updateState(SendState.Idle)
        smartSelectionCoordinator.release()
        currentState.close()
    }

    private fun setLoading(isLoading: Boolean) {
        val loadingState = FeeLoadingState.Instant(isLoading = isLoading)
        updateState(Loading(loadingState))
    }

    private fun createFeeTotal(): SendFeeTotal {
        val solanaFee = smartSelectionCoordinator.getFeeData()?.let {
            SendSolanaFee(it.first, it.second, sourceToken)
        }

        val currentAmount = inputCalculator.getCurrentAmount()
        return SendFeeTotal(
            currentAmount = currentAmount,
            currentAmountUsd = inputCalculator.getCurrentAmountUsd(),
            receive = "${currentAmount.formatToken()} ${sourceToken.tokenSymbol}",
            receiveUsd = currentAmount.toUsd(sourceToken),
            sourceSymbol = sourceToken.tokenSymbol,
            sendFee = solanaFee,
            recipientAddress = initialData.recipient.address,
            feeLimit = feeLimitInfo
        )
    }

    private fun updateState(newState: SendState) {
        launch {
            Timber.tag("SendPresenter").i("UpdateCommonState SendCommonState: $newState")
            currentState.send(newState)
        }
    }

    private fun buildTransaction(transactionId: String): HistoryTransaction =
        RpcHistoryTransaction.Transfer(
            signature = transactionId,
            date = ZonedDateTime.now(),
            blockNumber = -1,
            type = RpcHistoryTransactionType.SEND,
            senderAddress = tokenKeyProvider.publicKey,
            amount = RpcHistoryAmount(inputCalculator.getCurrentAmount(), inputCalculator.getCurrentAmountUsd()),
            destination = initialData.recipient.address,
            counterPartyUsername = initialData.recipient.nicknameOrAddress(),
            fees = null,
            status = HistoryTransactionStatus.PENDING,
            iconUrl = sourceToken.iconUrl,
            symbol = sourceToken.tokenSymbol
        )

    private fun logSendError(error: Throwable, feePayer: Token.Active, solanaFee: SendSolanaFee?) {
        launch {
            val accountCreationFee = solanaFee?.accountCreationFeeDecimals?.toPlainString()
            val transactionFee = solanaFee?.transactionDecimals?.toPlainString()
            alertErrorsLogger.triggerSendAlarm(
                token = sourceToken,
                currency = inputCalculator.currencyMode.getTypedSymbol(),
                amount = inputCalculator.getCurrentAmount().toPlainString(),
                feePayerToken = feePayer,
                accountCreationFee = accountCreationFee,
                transactionFee = transactionFee,
                relayAccount = sendInteractor.getUserRelayAccount(),
                recipientAddress = initialData.recipient,
                error = error
            )
        }
    }

    private fun logSendClicked(solanaFee: SendSolanaFee?, amountInToken: String, amountInUsd: String) {
        newSendAnalytics.logSendConfirmButtonClicked(
            tokenName = sourceToken.tokenName,
            amountInToken = amountInToken,
            amountInUsd = amountInUsd,
            isFeeFree = solanaFee?.isTransactionFree ?: false,
            mode = inputCalculator.currencyMode
        )
    }
}

package org.p2p.wallet.newsend.smartselection

import timber.log.Timber
import java.math.BigInteger
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.token.findByMintAddress
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.toUsd
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.CalculationState
import org.p2p.wallet.newsend.model.FeeLoadingState
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.SendActionEvent
import org.p2p.wallet.newsend.model.SendFeeTotal
import org.p2p.wallet.newsend.model.SendSolanaFee
import org.p2p.wallet.newsend.model.SendState
import org.p2p.wallet.newsend.model.SendState.GeneralError
import org.p2p.wallet.newsend.model.SendState.Loading
import org.p2p.wallet.newsend.model.main.WidgetState
import org.p2p.wallet.newsend.smartselection.initial.SendInitialData
import org.p2p.wallet.newsend.ui.main.SendInputCalculator
import org.p2p.wallet.user.interactor.UserInteractor

private const val TAG = "SendFeeRelayerManager"

class SendStateManager(
    private val initialData: SendInitialData,
    private val sendInteractor: SendInteractor,
    private val userInteractor: UserInteractor,
    private val smartSelectionCoordinator: SmartSelectionCoordinator,
    private val inputCalculator: SendInputCalculator,
    dispatchers: CoroutineDispatchers
) : CoroutineScope {

    override val coroutineContext: CoroutineContext =
        SupervisorJob() + dispatchers.io + Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val currentState: MutableStateFlow<SendState> = MutableStateFlow(SendState.Idle)

    private lateinit var feeLimitInfo: TransactionFeeLimits
    private lateinit var solToken: Token.Active
    private lateinit var sourceToken: Token.Active

    private var observeTokensJob: Job? = null

    init {
        observeInternalStates()
    }

    fun getStateFlow(): StateFlow<SendState> = currentState.asStateFlow()

    fun onNewEvent(newEvent: SendActionEvent) {
        when (newEvent) {
            is SendActionEvent.InitialLoading -> executeInitialLoading()
            is SendActionEvent.AmountChanged -> handleAmountChanged(newEvent)
            is SendActionEvent.SourceTokenChanged -> handleSourceTokenChanged(newEvent)
            is SendActionEvent.FeePayerManuallyChanged -> handleFeePayerChanged(newEvent)
            is SendActionEvent.MaxAmountEntered -> handleMaxAmountEntered()
            is SendActionEvent.CurrencyModeSwitched -> handleCurrencyModeSwitched()
            is SendActionEvent.OnFeeClicked -> handleFeeClicked()
            is SendActionEvent.OnTokenClicked -> handleTokenClicked()
        }
    }

    // Load or restore the initial send state
    private fun executeInitialLoading() {
        launch {
            val userTokens = userInteractor.getNonZeroUserTokens()
            validateTokenSelection(userTokens)

            if (::sourceToken.isInitialized) {
                restoreScreen()
            } else {
                initializeScreen(userTokens)
            }

            val trigger = SmartSelectionTrigger.Initialization(
                initialToken = sourceToken,
                initialAmount = initialData.inputAmount
            )
            smartSelectionCoordinator.setInitialFeePayer(sourceToken)
            smartSelectionCoordinator.onNewTrigger(trigger)

            observeTokenUpdates(sourceToken)
            validateCurrencySwitch()
            validateInput()
        }
    }

    private fun restoreScreen() {
        updateToken(sourceToken)


    }

    private suspend fun initializeScreen(userTokens: List<Token.Active>) {
        try {
            setLoading(isLoading = true)

            sourceToken = initialData.selectInitialToken(userTokens)
            solToken = initialData.findSolToken(sourceToken, userInteractor.getUserSolToken())

            feeLimitInfo = sendInteractor.getFreeTransactionsInfo(useCache = false)
            sendInteractor.getMinRelayRentExemption().also { inputCalculator.saveMinRentExemption(it) }
            sendInteractor.initialize()
        } catch (e: Throwable) {
            Timber.tag(TAG).i(e, "Send initial loading failed")
            updateState(GeneralError(e))
        } finally {
            setLoading(isLoading = false)
        }
    }

    private fun handleAmountChanged(event: SendActionEvent.AmountChanged) {
        inputCalculator.updateInputAmount(event.newInputAmount)

        val trigger = SmartSelectionTrigger.AmountChanged(
            solToken = solToken,
            sourceToken = sourceToken,
            inputAmount = event.newInputAmount.toBigDecimalOrNull(),
        )
        smartSelectionCoordinator.onNewTrigger(trigger)
    }

    private fun handleSourceTokenChanged(event: SendActionEvent.SourceTokenChanged) {
        updateToken(updatedToken = event.newSourceToken)

        validateCurrencySwitch()

        val trigger = SmartSelectionTrigger.SourceTokenChanged(
            solToken = solToken,
            newSourceToken = event.newSourceToken,
            inputAmount = inputCalculator.getCurrentAmount()
        )
        smartSelectionCoordinator.onNewTrigger(trigger)
    }

    private fun handleFeePayerChanged(event: SendActionEvent.FeePayerManuallyChanged) {
        val trigger = SmartSelectionTrigger.FeePayerManuallyChanged(
            sourceToken = sourceToken,
            newFeePayer = event.newFeePayerToken
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
        if (smartSelectionCoordinator.isTransactionFree()) {
            updateState(SendState.ShowFreeTransactionDetails)
        } else {
            updateState(SendState.ShowTransactionDetails(createFeeTotal()))
        }
    }

    private fun observeInternalStates() {
        merge(
            smartSelectionCoordinator.getFeePayerStateFlow(),
            inputCalculator.getStateFlow()
        )
            .onEach { state -> onInternalStateUpdated(state) }
            .launchIn(this)
    }

    private fun onInternalStateUpdated(state: Any) {
        when (state) {
            is FeePayerState -> {
                if (state is FeePayerState.ReduceAmount) {
                    inputCalculator.reduceAmount(state.newInputAmount)
                }
                currentState.value = SendState.FeePayerUpdate(state)
            }
            is CalculationState -> {
                currentState.value = SendState.CalculationUpdate(state)
            }
            else -> Unit
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

    fun getState(): SendState = currentState.value

    suspend fun sendTransaction(destinationAddress: PublicKey, token: Token.Active, lamports: BigInteger): String =
        sendInteractor.sendTransaction(destinationAddress, token, lamports)

    private fun buildSolanaFee(
        newFeePayer: Token.Active,
        source: Token.Active,
        feeRelayerFee: FeeRelayerFee
    ): SendSolanaFee {
        return SendSolanaFee(
            feePayerToken = newFeePayer,
            feeRelayerFee = feeRelayerFee,
            sourceToken = source
        )
    }

    fun release() {
        updateState(SendState.Idle)
        smartSelectionCoordinator.release()
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
            recipientAddress = initialData.recipient.addressState.address,
            feeLimit = feeLimitInfo
        )
    }

    private fun updateState(newState: SendState) {
        currentState.value = newState
    }
}

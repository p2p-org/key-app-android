package org.p2p.wallet.newsend

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates.observable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.CORRECT_AMOUNT
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.NO_ACTION
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.SELECT_FEE_PAYER
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.CalculationMode
import org.p2p.wallet.newsend.model.FeeLoadingState
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.FeeRelayerState
import org.p2p.wallet.newsend.model.FeeRelayerState.Failure
import org.p2p.wallet.newsend.model.FeeRelayerState.ReduceAmount
import org.p2p.wallet.newsend.model.FeeRelayerState.UpdateFee
import org.p2p.wallet.newsend.model.FeeRelayerStateError
import org.p2p.wallet.newsend.model.FeeRelayerStateError.FeesCalculationError
import org.p2p.wallet.newsend.model.FeeRelayerStateError.InsufficientFundsToCoverFees
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.model.SendFeeTotal
import org.p2p.wallet.newsend.model.SendSolanaFee
import org.p2p.wallet.newsend.smartselection.FeePayerSelector

private const val TAG = "SendFeeRelayerManager"

class SendFeeRelayerManager(
    private val sendInteractor: SendInteractor,
    private val feePayerSelector: FeePayerSelector,
    dispatchers: CoroutineDispatchers
) : CoroutineScope {

    override val coroutineContext: CoroutineContext =
        SupervisorJob() + dispatchers.io + Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    var onStateUpdated: ((FeeRelayerState) -> Unit)? = null
    var onFeeLoading: ((FeeLoadingState) -> Unit)? = null

    private var currentState: FeeRelayerState by observable(FeeRelayerState.Idle) { _, _, newState ->
        onStateUpdated?.invoke(newState)
    }

    private var currentStrategy: FeePayerSelectionStrategy = SELECT_FEE_PAYER

    private lateinit var feeLimitInfo: TransactionFeeLimits
    private lateinit var recipientAddress: SearchResult
    private lateinit var solToken: Token.Active

    private var initializeCompleted = false
    private var minRentExemption: BigInteger = BigInteger.ZERO

    private val alternativeTokens: HashMap<String, List<Token.Active>> = HashMap()

    init {
        launch {
            feePayerSelector.getFeePayerStateFlow()
                .stateIn(this)
                .collectLatest { state -> handleFeePayerState(state) }
        }

        launch {
        }
    }

    suspend fun initialize(
        initialToken: Token.Active,
        solToken: Token.Active,
        recipientAddress: SearchResult
    ) {
        Timber.tag(TAG).i("initialize for SendFeeRelayerManager")
        this.recipientAddress = recipientAddress
        this.solToken = solToken

        onFeeLoading?.invoke(FeeLoadingState.Instant(isLoading = true))
        try {
            initializeWithToken(initialToken)
            initializeCompleted = true
        } catch (e: Throwable) {
            Timber.tag(TAG).i(e, "initialize for SendFeeRelayerManager failed")
            initializeCompleted = false
            handleError(FeesCalculationError(e))
        } finally {
            onFeeLoading?.invoke(FeeLoadingState.Instant(isLoading = false))
        }
    }

    private suspend fun initializeWithToken(initialToken: Token.Active) {
        Timber.tag(TAG).i("initialize for SendFeeRelayerManager with token ${initialToken.mintAddress}")
        minRentExemption = sendInteractor.getMinRelayRentExemption()
        feeLimitInfo = sendInteractor.getFreeTransactionsInfo()
        sendInteractor.initialize(initialToken)
    }

    fun updateFeePayer(newToken: Token.Active) {
        sendInteractor.setFeePayerToken(newToken)
    }

    fun getFeePayerToken(): Token.Active = sendInteractor.getFeePayerToken()

    suspend fun getUserRelayAccount(): RelayAccount = sendInteractor.getUserRelayAccount()

    fun getMinRentExemption(): BigInteger = minRentExemption

    fun getState(): FeeRelayerState = currentState

    fun buildTotalFee(
        sourceToken: Token.Active,
        calculationMode: CalculationMode,
    ): SendFeeTotal {
        val currentAmount = calculationMode.getCurrentAmount()
        return SendFeeTotal(
            currentAmount = currentAmount,
            currentAmountUsd = calculationMode.getCurrentAmountUsd(),
            receive = "${currentAmount.formatToken()} ${sourceToken.tokenSymbol}",
            receiveUsd = currentAmount.toUsd(sourceToken),
            sourceSymbol = sourceToken.tokenSymbol,
            sendFee = (currentState as? UpdateFee)?.solanaFee,
            recipientAddress = recipientAddress.addressState.address,
            feeLimit = feeLimitInfo
        )
    }

    /**
     * Launches the auto-selection mechanism
     * Selects automatically the fee payer token if there is enough balance
     * */
    suspend fun executeSmartSelection(
        sourceToken: Token.Active,
        feePayerToken: Token.Active?,
        tokenAmount: BigDecimal,
        useCache: Boolean,
        strategy: FeePayerSelectionStrategy
    ) {

        // if user selected a fee payer manually then we are not launching the smart selection
        if (currentStrategy == NO_ACTION && strategy != NO_ACTION) return

        currentStrategy = strategy

        val feePayer = feePayerToken ?: sendInteractor.getFeePayerToken()

        try {
            onFeeLoading?.invoke(FeeLoadingState(isLoading = true, isDelayed = useCache))
            if (!initializeCompleted) {
                initializeWithToken(sourceToken)
                initializeCompleted = true
            }

            val feeState = calculateFeeRelayerFee(
                sourceToken = sourceToken,
                feePayerToken = feePayer,
                result = recipientAddress,
                useCache = useCache
            )

            when (feeState) {
                is FeeCalculationState.NoFees -> {
                    currentState = UpdateFee(solanaFee = null, feeLimitInfo = feeLimitInfo)
                    sendInteractor.setFeePayerToken(sourceToken)
                }

                is FeeCalculationState.PoolsNotFound -> {
                    val solanaFee = buildSolanaFee(
                        newFeePayer = solToken,
                        source = sourceToken,
                        feeRelayerFee = feeState.feeInSol
                    )
                    currentState = UpdateFee(solanaFee = solanaFee, feeLimitInfo = feeLimitInfo)
                    sendInteractor.switchFeePayerToSol(solToken)
                }

                is FeeCalculationState.Success -> {
                    sendInteractor.setFeePayerToken(feePayer)
                    val inputAmount = tokenAmount.toLamports(sourceToken.decimals)
                    handleFeeCalculated(
                        sourceToken = sourceToken,
                        feeRelayerFee = feeState.fee,
                        feePayerToken = feePayer,
                        inputAmount = inputAmount
                    )
                }

                is FeeCalculationState.Error -> {
                    Timber.tag(TAG).e(feeState.error, "Error during FeeRelayer fee calculation")
                    handleError(FeesCalculationError(feeState.error))
                }

                is FeeCalculationState.Cancelled -> Unit
            }
        } catch (e: CancellationException) {
            Timber.tag(TAG).i("Smart selection job was cancelled")
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error during FeeRelayer fee calculation")
        } finally {
            onFeeLoading?.invoke(FeeLoadingState(isLoading = false, isDelayed = useCache))
        }
    }

    suspend fun buildDebugInfo(solanaFee: SendSolanaFee?): String {
        val relayAccount = sendInteractor.getUserRelayAccount()
        val relayInfo = sendInteractor.getRelayInfo()
        return buildString {
            append("Relay account is created: ${relayAccount.isCreated}, balance: ${relayAccount.balance} (A)")
            appendLine()
            append("Min relay account balance required: ${relayInfo.minimumRelayAccountRent} (B)")
            appendLine()
            if (relayAccount.balance != null) {
                val diff = relayAccount.balance - relayInfo.minimumRelayAccountRent
                append("Remainder (A - B): $diff (R)")
                appendLine()
            }

            if (solanaFee == null) {
                append("Expected total fee in SOL: 0 (E)")
                appendLine()
                append("Needed top up amount (E - R): 0 (S)")
                appendLine()
                append("Expected total fee in Token: 0 (T)")
            } else {
                val accountBalances = solanaFee.feeRelayerFee.expectedFee.accountBalances
                val expectedFee = if (!relayAccount.isCreated) {
                    accountBalances + relayInfo.minimumRelayAccountRent
                } else {
                    accountBalances
                }
                append("Expected total fee in SOL: $expectedFee (E)")
                appendLine()

                val neededTopUpAmount = solanaFee.feeRelayerFee.totalInSol
                append("Needed top up amount (E - R): $neededTopUpAmount (S)")

                appendLine()

                val feePayerToken = solanaFee.feePayerToken
                val expectedFeeInSpl = solanaFee.feeRelayerFee.totalInSpl.orZero()
                    .fromLamports(feePayerToken.decimals)
                    .scaleLong()
                append("Expected total fee in Token: $expectedFeeInSpl ${feePayerToken.tokenSymbol} (T)")
            }
        }
    }

    suspend fun sendTransaction(destinationAddress: PublicKey, token: Token.Active, lamports: BigInteger): String =
        sendInteractor.sendTransaction(destinationAddress, token, lamports)

    /*
     * Assume this to be called only if associated account address creation needed
     * */
    private suspend fun calculateFeeRelayerFee(
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        result: SearchResult,
        useCache: Boolean = true
    ): FeeCalculationState {
        val recipient = result.addressState.address
        return sendInteractor.calculateFeesForFeeRelayer(
            feePayerToken = feePayerToken,
            token = sourceToken,
            recipient = recipient,
            useCache = useCache
        )
    }

    private fun handleFeeCalculated(
        sourceToken: Token.Active,
        feeRelayerFee: FeeRelayerFee,
        feePayerToken: Token.Active,
        inputAmount: BigInteger
    ) {
        val fee = buildSolanaFee(feePayerToken, sourceToken, feeRelayerFee)
        currentState = UpdateFee(fee, feeLimitInfo)

        when (currentStrategy) {
            NO_ACTION -> {
                validateFunds(sourceToken, fee, inputAmount)
            }
            SELECT_FEE_PAYER -> {
                feePayerSelector.execute(sourceToken, fee, inputAmount, alternativeTokens, isCorrectableAmount = false)
            }
            CORRECT_AMOUNT -> {
                feePayerSelector.execute(sourceToken, fee, inputAmount, alternativeTokens, isCorrectableAmount = true)
            }
        }
    }

    private fun validateFunds(source: Token.Active, fee: SendSolanaFee, inputAmount: BigInteger) {
        val isEnoughToCoverExpenses = fee.isEnoughToCoverExpenses(
            sourceTokenTotal = source.totalInLamports,
            inputAmount = inputAmount,
            minRentExemption = minRentExemption
        )

        if (!isEnoughToCoverExpenses) {
            handleError(InsufficientFundsToCoverFees)
        }
    }

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

    private fun handleFeePayerState(newState: FeePayerState) {
        when (newState) {
            is FeePayerState.Idle -> {
                Timber.tag(TAG).i("Fee payer state is idle")
            }
            is FeePayerState.SwitchToSpl -> {
                Timber.tag(TAG).i("Switching to SPL -> ${newState.tokenToSwitch.tokenSymbol}")
                sendInteractor.setFeePayerToken(newState.tokenToSwitch)
            }

            is FeePayerState.SwitchToSol -> {
                Timber.tag(TAG).i(
                    "Switching to SOL -> ${solToken.tokenSymbol}"
                )
                sendInteractor.switchFeePayerToSol(solToken)
            }

            is FeePayerState.ReduceInputAmount -> {
                Timber.tag(TAG).i("Reducing amount to ${newState.maxAllowedAmount}")
                sendInteractor.setFeePayerToken(newState.sourceToken)
                currentState = ReduceAmount(newState.fee, newState.maxAllowedAmount)
            }
        }

//        recalculate(sourceToken, inputAmount)
    }

    private suspend fun recalculate(sourceToken: Token.Active, inputAmount: BigInteger) {
        /*
         * Optimized recalculation and UI update
         * */
        val newFeePayer = sendInteractor.getFeePayerToken()
        val feeState = try {
            calculateFeeRelayerFee(
                sourceToken = sourceToken,
                feePayerToken = newFeePayer,
                result = recipientAddress
            )
        } catch (e: CancellationException) {
            Timber.tag(TAG).i("Fee calculation is cancelled")
            null
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error calculating fees")
            handleError(FeesCalculationError(e))
            null
        }

        when (feeState) {
            is FeeCalculationState.NoFees -> {
                currentState = UpdateFee(solanaFee = null, feeLimitInfo = feeLimitInfo)
            }

            is FeeCalculationState.PoolsNotFound -> {
                val solanaFee = buildSolanaFee(solToken, sourceToken, feeState.feeInSol)
                currentState = UpdateFee(solanaFee = solanaFee, feeLimitInfo = feeLimitInfo)
                sendInteractor.setFeePayerToken(solToken)
            }

            is FeeCalculationState.Success -> {
                val fee = buildSolanaFee(newFeePayer, sourceToken, feeState.fee)
                validateFunds(sourceToken, fee, inputAmount)
                currentState = UpdateFee(fee, feeLimitInfo)
            }

            is FeeCalculationState.Error -> {
                handleError(FeesCalculationError(cause = feeState.error))
            }

            else -> Unit
        }
    }

    private fun handleError(error: FeeRelayerStateError) {
        val previousState = currentState
        currentState = Failure(previousState, error)
    }

    fun release() {
        currentState = FeeRelayerState.Idle
        initializeCompleted = false
    }
}

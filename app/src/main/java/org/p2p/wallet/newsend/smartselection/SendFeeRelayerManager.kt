package org.p2p.wallet.newsend.smartselection

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.p2p.core.token.Token
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.feerelayer.interactor.FeeRelayerCalculationInteractor
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.CORRECT_AMOUNT
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy.MANUAL
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
import org.p2p.wallet.newsend.model.FeeRelayerState.ReduceAmount
import org.p2p.wallet.newsend.model.FeeRelayerState.InsufficientFundsError
import org.p2p.wallet.newsend.model.FeeRelayerState.UpdateFee
import org.p2p.wallet.newsend.model.FeeRelayerState.FeeError
import org.p2p.wallet.newsend.model.FeeRelayerState.Loading
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.model.SendFeeTotal
import org.p2p.wallet.newsend.model.SendSolanaFee

private const val TAG = "SendFeeRelayerManager"

class SendFeeRelayerManager(
    private val sendInteractor: SendInteractor,
    private val feePayerSelector: FeePayerSelector,
    private val feeCalculationInteractor: FeeRelayerCalculationInteractor,
    dispatchers: CoroutineDispatchers
) : CoroutineScope {

    override val coroutineContext: CoroutineContext =
        SupervisorJob() + dispatchers.io + Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val currentState: MutableStateFlow<FeeRelayerState> = MutableStateFlow(FeeRelayerState.Idle)

    private var currentStrategy: FeePayerSelectionStrategy = SELECT_FEE_PAYER

    private lateinit var feeLimitInfo: TransactionFeeLimits
    private lateinit var recipientAddress: SearchResult
    private lateinit var solToken: Token.Active

    private var initializeCompleted = false
    private var minRentExemption: BigInteger = BigInteger.ZERO

    init {
        launch {
            feePayerSelector.getFeePayerStateFlow()
                .stateIn(this)
                .collectLatest { state -> handleFeePayerState(state) }
        }
    }

    fun getStateFlow(): Flow<FeeRelayerState> = currentState

    suspend fun initialize(
        initialToken: Token.Active,
        solToken: Token.Active,
        recipientAddress: SearchResult
    ) {
        Timber.tag(TAG).i("initialize for SendFeeRelayerManager")
        this.recipientAddress = recipientAddress
        this.solToken = solToken

        val loadingState = FeeLoadingState.Instant(isLoading = true)
        updateState(Loading(loadingState))
        try {
            initializeWithToken(initialToken)
            initializeCompleted = true
        } catch (e: Throwable) {
            Timber.tag(TAG).i(e, "initialize for SendFeeRelayerManager failed")
            initializeCompleted = false
            updateState(FeeError(e))
        } finally {
            val finalState = FeeLoadingState.Instant(isLoading = false)
            updateState(Loading(finalState))
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

    fun getState(): FeeRelayerState = currentState.value

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
            sendFee = (currentState.value as? UpdateFee)?.solanaFee,
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
        newStrategy: FeePayerSelectionStrategy
    ) {

        // if user selected a fee payer manually then we are not launching the smart selection
        if (currentStrategy == MANUAL && newStrategy != MANUAL) return

        currentStrategy = newStrategy

        val feePayer = feePayerToken ?: sendInteractor.getFeePayerToken()

        try {
            val loadingState = FeeLoadingState(isLoading = true, isDelayed = useCache)
            updateState(Loading(loadingState))
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
                    updateState(UpdateFee(solanaFee = null, feeLimitInfo = feeLimitInfo))
                    sendInteractor.setFeePayerToken(sourceToken)
                }

                is FeeCalculationState.PoolsNotFound -> {
                    val solanaFee = buildSolanaFee(
                        newFeePayer = solToken,
                        source = sourceToken,
                        feeRelayerFee = feeState.feeInSol
                    )
                    updateState(UpdateFee(solanaFee = solanaFee, feeLimitInfo = feeLimitInfo))
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
                    updateState(FeeError(feeState.error))
                }

                is FeeCalculationState.Cancelled -> Unit
            }
        } catch (e: CancellationException) {
            Timber.tag(TAG).i("Smart selection job was cancelled")
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error during FeeRelayer fee calculation")
        } finally {
            val finalLoadingState = FeeLoadingState(isLoading = false, isDelayed = useCache)
            updateState(Loading(finalLoadingState))
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
        return feeCalculationInteractor.calculateFeesForFeeRelayer(
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
        updateState(UpdateFee(fee, feeLimitInfo))

        when (currentStrategy) {
            MANUAL -> {
                validateFunds(sourceToken, fee, inputAmount)
            }
            SELECT_FEE_PAYER -> {
                feePayerSelector.execute(sourceToken, fee, inputAmount, isCorrectableAmount = false)
            }
            CORRECT_AMOUNT -> {
                feePayerSelector.execute(sourceToken, fee, inputAmount, isCorrectableAmount = true)
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
            updateState(InsufficientFundsError)
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
            is FeePayerState.Empty -> {
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
                updateState(ReduceAmount(newState.fee, newState.maxAllowedAmount))
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
            updateState(FeeError(e))
            null
        }

        when (feeState) {
            is FeeCalculationState.NoFees -> {
                updateState(UpdateFee(solanaFee = null, feeLimitInfo = feeLimitInfo))
            }

            is FeeCalculationState.PoolsNotFound -> {
                val solanaFee = buildSolanaFee(solToken, sourceToken, feeState.feeInSol)
                updateState(UpdateFee(solanaFee = solanaFee, feeLimitInfo = feeLimitInfo))
                sendInteractor.setFeePayerToken(solToken)
            }

            is FeeCalculationState.Success -> {
                val fee = buildSolanaFee(newFeePayer, sourceToken, feeState.fee)
                validateFunds(sourceToken, fee, inputAmount)
                updateState(UpdateFee(fee, feeLimitInfo))
            }

            is FeeCalculationState.Error -> {
                updateState(FeeError(feeState.error))
            }

            else -> Unit
        }
    }

    fun release() {
        updateState(FeeRelayerState.Idle)
        initializeCompleted = false
    }

    private fun updateState(newState: FeeRelayerState) {
        currentState.value = newState
    }
}

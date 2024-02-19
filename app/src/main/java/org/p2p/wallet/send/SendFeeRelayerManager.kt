package org.p2p.wallet.send

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.properties.Delegates.observable
import kotlinx.coroutines.CancellationException
import org.p2p.core.token.Token
import org.p2p.core.utils.formatTokenWithSymbol
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig.Companion.interestBearingConfig
import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig.Companion.transferFeeConfig
import org.p2p.solanaj.kits.TokenExtensionsMap
import org.p2p.solanaj.rpc.RpcSolanaRepository
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits
import org.p2p.wallet.send.interactor.SendInteractor
import org.p2p.wallet.send.interactor.usecase.CalculateSendFeesUseCase
import org.p2p.wallet.send.interactor.usecase.CalculateToken2022TransferFeeUseCase
import org.p2p.wallet.send.interactor.usecase.GetTokenExtensionsUseCase
import org.p2p.wallet.send.model.CalculationMode
import org.p2p.wallet.send.model.FeeLoadingState
import org.p2p.wallet.send.model.FeePayerState
import org.p2p.wallet.send.model.FeeRelayerState
import org.p2p.wallet.send.model.FeeRelayerState.Failure
import org.p2p.wallet.send.model.FeeRelayerState.ReduceAmount
import org.p2p.wallet.send.model.FeeRelayerState.UpdateFee
import org.p2p.wallet.send.model.FeeRelayerStateError
import org.p2p.wallet.send.model.FeeRelayerStateError.FeesCalculationError
import org.p2p.wallet.send.model.FeeRelayerStateError.InsufficientFundsToCoverFees
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.model.SendFeeTotal
import org.p2p.wallet.send.model.SendSolanaFee
import org.p2p.wallet.user.interactor.UserInteractor

private const val TAG = "SendFeeRelayerManager"

class SendFeeRelayerManager(
    private val sendInteractor: SendInteractor,
    private val userInteractor: UserInteractor,
    private val solanaRepository: RpcSolanaRepository,
    private val getTokenExtensionsUseCase: GetTokenExtensionsUseCase,
    private val calculateToken2022TransferFeeUseCase: CalculateToken2022TransferFeeUseCase,
    private val calculateSendFeesUseCase: CalculateSendFeesUseCase,
) {

    var onStateUpdated: ((FeeRelayerState) -> Unit)? = null
    var onFeeLoading: ((FeeLoadingState) -> Unit)? = null

    private var currentState: FeeRelayerState by observable(FeeRelayerState.Idle) { _, oldState, newState ->
        Timber.tag(TAG).i(
            "Switching send fee relayer state to ${oldState.javaClass.simpleName} to ${newState.javaClass.simpleName}"
        )
        onStateUpdated?.invoke(newState)
    }

    private lateinit var feeLimitInfo: TransactionFeeLimits
    private lateinit var recipientAddress: SearchResult
    private lateinit var solToken: Token.Active
    private var initializeCompleted = false
    private var currentSolanaEpoch: BigInteger = BigInteger.ZERO

    private var minRentExemption: BigInteger = BigInteger.ZERO

    private val alternativeTokensMap: HashMap<String, List<Token.Active>> = HashMap()

    private val TokenExtensionsMap.actualTransferFeePercent: BigDecimal
        get() {
            return this.transferFeeConfig
                ?.getActualTransferFee(currentSolanaEpoch)
                ?.transferFeePercent
                ?: BigDecimal.ZERO
        }

    suspend fun initialize(
        feePayerToken: Token.Active,
        solToken: Token.Active,
        recipientAddress: SearchResult
    ) {
        Timber.tag(TAG).i("initialize for SendFeeRelayerManager")
        this.recipientAddress = recipientAddress
        this.solToken = solToken

        onFeeLoading?.invoke(FeeLoadingState.Instant(isLoading = true))
        try {
            initializeWithToken(feePayerToken)

            initializeCompleted = true
        } catch (e: Throwable) {
            Timber.tag(TAG).i(e, "initialize for SendFeeRelayerManager failed")
            initializeCompleted = false
            handleError(FeesCalculationError(e))
        } finally {
            onFeeLoading?.invoke(FeeLoadingState.Instant(isLoading = false))
        }
    }

    private suspend fun initializeWithToken(feePayerToken: Token.Active) {
        Timber.tag(TAG).i("initialize for SendFeeRelayerManager with token ${feePayerToken.mintAddress}")
        minRentExemption = sendInteractor.getMinRelayRentExemption()
        feeLimitInfo = sendInteractor.getFreeTransactionsInfo()
        currentSolanaEpoch = solanaRepository.getEpochInfo(useCache = true).epoch
        sendInteractor.initialize(feePayerToken)
    }

    fun getMinRentExemption(): BigInteger = minRentExemption

    fun getState(): FeeRelayerState = currentState

    fun buildTotalFee(
        sourceToken: Token.Active,
        calculationMode: CalculationMode,
        tokenExtensions: TokenExtensionsMap,
    ): SendFeeTotal {
        val currentAmount = calculationMode.getCurrentAmount()

        val transferFeePercent: BigDecimal? = tokenExtensions
            .transferFeeConfig
            ?.getActualTransferFee(currentSolanaEpoch)
            ?.transferFeePercent

        val interestBearingPercent: BigDecimal? = tokenExtensions
            .interestBearingConfig
            ?.currentRate
            ?.toBigDecimal()

        return SendFeeTotal(
            currentAmount = currentAmount,
            currentAmountUsd = calculationMode.getCurrentAmountUsd(),
            receiveFormatted = currentAmount.formatTokenWithSymbol(
                tokenSymbol = sourceToken.tokenSymbol,
                decimals = sourceToken.decimals
            ),
            receiveUsd = currentAmount.toUsd(sourceToken),
            sourceSymbol = sourceToken.tokenSymbol,
            sendFee = (currentState as? UpdateFee)?.solanaFee,
            recipientAddress = recipientAddress.address,
            feeLimit = feeLimitInfo,
            transferFeePercent = transferFeePercent,
            interestBearingPercent = interestBearingPercent
        )
    }

    /**
     * Launches the auto-selection mechanism
     * Selects automatically the fee payer token if there is enough balance
     * */
    suspend fun executeSmartSelection(
        sourceToken: Token.Active,
        feePayerToken: Token.Active?,
        strategy: FeePayerSelectionStrategy,
        tokenAmount: BigDecimal,
        useCache: Boolean
    ) {
        val feePayer = feePayerToken ?: sendInteractor.getFeePayerToken()

        val tokenExtensions = getTokenExtensionsUseCase.execute(sourceToken.mintAddress)
        val token2022TransferFee = calculateToken2022TransferFeeUseCase.execute(sourceToken, tokenAmount)

        try {
            onFeeLoading?.invoke(FeeLoadingState(isLoading = true, isDelayed = useCache))
            if (!initializeCompleted) {
                initializeWithToken(feePayer)
                initializeCompleted = true
            }

            val feeState = calculateFees(
                sourceToken = sourceToken,
                feePayerToken = feePayer,
                result = recipientAddress,
                useCache = useCache
            )

            when (feeState) {
                is FeeCalculationState.NoFees -> {
                    currentState = UpdateFee(
                        solanaFee = null,
                        feeLimitInfo = feeLimitInfo,
                        tokenExtensions = tokenExtensions
                    )
                    sendInteractor.setFeePayerToken(feePayer)
                }
                is FeeCalculationState.PoolsNotFound -> {
                    val solanaFee = buildSolanaFee(
                        newFeePayer = solToken,
                        source = sourceToken,
                        feeRelayerFee = feeState.feeInSol,
                        token2022TransferFee = token2022TransferFee,
                        token2022TransferFeePercent = tokenExtensions.actualTransferFeePercent
                    )
                    currentState = UpdateFee(
                        solanaFee = solanaFee,
                        feeLimitInfo = feeLimitInfo,
                        tokenExtensions = tokenExtensions,
                    )
                    sendInteractor.setFeePayerToken(solToken)
                }
                is FeeCalculationState.Success -> {
                    sendInteractor.setFeePayerToken(feePayer)
                    val inputAmount = tokenAmount.toLamports(sourceToken.decimals)
                    setFeeDetailsState(
                        sourceToken = sourceToken,
                        feeRelayerFee = feeState.fee,
                        feePayerToken = feePayer,
                        inputAmount = inputAmount,
                        strategy = strategy,
                        token2022TransferFee = token2022TransferFee,
                        tokenExtensions = tokenExtensions
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

    fun buildDebugInfo(solanaFee: SendSolanaFee?): String {
        return buildString {
            if (solanaFee == null) {
                append("Expected total fee in SOL: 0 (E)")
                appendLine()
                append("Needed top up amount (E - R): 0 (S)")
                appendLine()
                append("Expected total fee in Token: 0 (T)")
            } else {
                val accountBalances = solanaFee.feeRelayerFee.expectedFee.accountCreationFee
                append("Expected total fee in SOL: $accountBalances (E)")
                appendLine()

                val neededTopUpAmount = solanaFee.feeRelayerFee.totalInSol
                append("Needed top up amount (E - R): $neededTopUpAmount (S)")
                appendLine()

                val feePayerToken = solanaFee.feePayerToken

                val feePayerTokenValue = solanaFee
                    .feeRelayerFee
                    .totalInFeePayerToken
                    .fromLamports(feePayerToken.decimals)
                    .formatTokenWithSymbol(
                        token = feePayerToken,
                        exactDecimals = true,
                        keepInitialDecimals = true
                    )

                val sourceToken = solanaFee.sourceToken
                val sourceTokenValue = solanaFee
                    .feeRelayerFee
                    .totalInSourceToken
                    .fromLamports(sourceToken.decimals)
                    .formatTokenWithSymbol(
                        token = sourceToken,
                        exactDecimals = true,
                        keepInitialDecimals = true
                    )

                append("Expected total fee in Fee Payer Token: $feePayerTokenValue (T)")
                appendLine()

                append("Expected total fee in Source Token: $sourceTokenValue (T)")
                appendLine()

                // todo: it is inaccurate when "max" is used
                append("[Token2022] Transfer Fee: ${solanaFee.token2022TransferFee.fromLamports(sourceToken.decimals)}")
            }
        }
    }

    /*
     * Assume this to be called only if associated account address creation needed
     * */
    private suspend fun calculateFees(
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        result: SearchResult,
        @Suppress("UNUSED_PARAMETER") useCache: Boolean = true
    ): FeeCalculationState = calculateSendFeesUseCase.execute(
        sourceToken = sourceToken,
        feePayerToken = feePayerToken,
        searchResult = result
    )

    private suspend fun setFeeDetailsState(
        sourceToken: Token.Active,
        feeRelayerFee: FeeRelayerFee,
        feePayerToken: Token.Active,
        token2022TransferFee: BigInteger,
        inputAmount: BigInteger,
        strategy: FeePayerSelectionStrategy,
        tokenExtensions: TokenExtensionsMap,
    ) {
        val fee = buildSolanaFee(
            newFeePayer = feePayerToken,
            source = sourceToken,
            feeRelayerFee = feeRelayerFee,
            token2022TransferFee = token2022TransferFee,
            token2022TransferFeePercent = tokenExtensions.actualTransferFeePercent
        )

        if (strategy == FeePayerSelectionStrategy.NO_ACTION) {
            validateFunds(sourceToken, fee, inputAmount)
            currentState = UpdateFee(
                solanaFee = fee,
                feeLimitInfo = feeLimitInfo,
                tokenExtensions = tokenExtensions,
            )
        } else {
            validateAndSelectFeePayer(
                sourceToken = sourceToken,
                fee = fee,
                inputAmount = inputAmount,
                strategy = strategy
            )
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

    private suspend fun buildSolanaFee(
        newFeePayer: Token.Active,
        source: Token.Active,
        feeRelayerFee: FeeRelayerFee,
        token2022TransferFee: BigInteger,
        token2022TransferFeePercent: BigDecimal,
    ): SendSolanaFee {
        val keyForAlternativeRequest = "${source.tokenSymbol}_${feeRelayerFee.totalInSol}"
        var alternativeTokens = alternativeTokensMap[keyForAlternativeRequest]
        if (alternativeTokens == null) {
            alternativeTokens = sendInteractor.findAlternativeFeePayerTokens(
                userTokens = userInteractor.getNonZeroUserTokens(),
                feePayerToExclude = newFeePayer,
                transactionFeeInSOL = feeRelayerFee.transactionFeeInSol,
                accountCreationFeeInSOL = feeRelayerFee.accountCreationFeeInSol
            )
            alternativeTokensMap[keyForAlternativeRequest] = alternativeTokens
        }
        return SendSolanaFee(
            feePayerToken = newFeePayer,
            solToken = solToken,
            feeRelayerFee = feeRelayerFee,
            alternativeFeePayerTokens = alternativeTokens,
            sourceToken = source,
            token2022TransferFee = token2022TransferFee,
            token2022TransferFeePercent = token2022TransferFeePercent
        )
    }

    private suspend fun validateAndSelectFeePayer(
        sourceToken: Token.Active,
        fee: SendSolanaFee,
        inputAmount: BigInteger,
        strategy: FeePayerSelectionStrategy
    ) {

        // Assuming token is not SOL
        val tokenTotal = sourceToken.total.toLamports(sourceToken.decimals)

        var amountToCheck = inputAmount

        /*
         * Checking if fee payer is SOL, otherwise fee payer is already correctly set up
         * - if there is enough SPL balance to cover fee, setting the default fee payer as SPL token
         * - if there is not enough SPL/SOL balance to cover fee, trying to reduce input amount
         * - In other cases, switching to SOL
         * */
        when (val state = fee.calculateFeePayerState(strategy, tokenTotal, inputAmount)) {
            is FeePayerState.SwitchToSpl -> {
                Timber.tag(TAG).i(
                    "Switching to SPL ${fee.feePayerToken.tokenSymbol} -> ${state.tokenToSwitch.tokenSymbol}"
                )
                sendInteractor.setFeePayerToken(state.tokenToSwitch)
            }
            is FeePayerState.SwitchToSol -> {
                Timber.tag(TAG).i(
                    "Switching to SOL ${fee.feePayerToken.tokenSymbol} -> ${solToken.tokenSymbol}"
                )
                sendInteractor.setFeePayerToken(solToken)
            }
            is FeePayerState.ReduceInputAmount -> {
                Timber.tag(TAG).i(
                    "Reducing amount $inputAmount for ${state.maxAllowedAmount}"
                )
                sendInteractor.setFeePayerToken(sourceToken)
                amountToCheck = state.maxAllowedAmount
                currentState = ReduceAmount(fee, state.maxAllowedAmount)
            }
            is FeePayerState.KeepSame -> {
                Timber.tag(TAG).i("Keeping the same fee payer token")
            }
        }

        recalculate(sourceToken, amountToCheck)
    }

    private suspend fun recalculate(sourceToken: Token.Active, inputAmount: BigInteger) {
        /*
         * Optimized recalculation and UI update
         * */
        val newFeePayer = sendInteractor.getFeePayerToken()
        val feeState = try {
            calculateFees(
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

        val tokenExtensions = getTokenExtensionsUseCase.execute(sourceToken.mintAddress)
        val token2022TransferFee = calculateToken2022TransferFeeUseCase.execute(sourceToken, inputAmount)

        when (feeState) {
            is FeeCalculationState.NoFees -> {
                currentState = UpdateFee(
                    solanaFee = null,
                    feeLimitInfo = feeLimitInfo,
                    tokenExtensions = tokenExtensions
                )
            }
            is FeeCalculationState.PoolsNotFound -> {
                val solanaFee = buildSolanaFee(
                    newFeePayer = solToken,
                    source = sourceToken,
                    feeRelayerFee = feeState.feeInSol,
                    token2022TransferFee = token2022TransferFee,
                    token2022TransferFeePercent = tokenExtensions.actualTransferFeePercent
                )
                currentState = UpdateFee(
                    solanaFee = solanaFee,
                    feeLimitInfo = feeLimitInfo,
                    tokenExtensions = tokenExtensions
                )
                sendInteractor.setFeePayerToken(solToken)
            }
            is FeeCalculationState.Success -> {
                val fee = buildSolanaFee(
                    newFeePayer = newFeePayer,
                    source = sourceToken,
                    feeRelayerFee = feeState.fee,
                    token2022TransferFee = token2022TransferFee,
                    token2022TransferFeePercent = tokenExtensions.actualTransferFeePercent
                )
                validateFunds(sourceToken, fee, inputAmount)
                currentState = UpdateFee(
                    solanaFee = fee,
                    feeLimitInfo = feeLimitInfo,
                    tokenExtensions = tokenExtensions
                )
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
}

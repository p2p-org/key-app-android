package org.p2p.wallet.newsend

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.properties.Delegates
import kotlinx.coroutines.CancellationException
import org.p2p.core.token.Token
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.orZero
import org.p2p.core.utils.scaleLong
import org.p2p.core.utils.toLamports
import org.p2p.core.utils.toUsd
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
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
import org.p2p.wallet.user.interactor.UserInteractor

class SendFeeRelayerManager(
    private val sendInteractor: SendInteractor,
    private val userInteractor: UserInteractor
) {

    var onStateUpdated: ((FeeRelayerState) -> Unit)? = null
    var onFeeLoading: ((FeeLoadingState) -> Unit)? = null

    private var currentState: FeeRelayerState by Delegates.observable(FeeRelayerState.Idle) { _, oldState, newState ->
        onStateUpdated?.invoke(newState)
    }

    private lateinit var feeLimitInfo: FreeTransactionFeeLimit
    private lateinit var recipientAddress: SearchResult
    private lateinit var solToken: Token.Active
    private var initializeCompleted = false

    private var minRentExemption: BigInteger = BigInteger.ZERO

    private val alternativeTokensMap: HashMap<String, List<Token.Active>> = HashMap()

    suspend fun initialize(
        initialToken: Token.Active,
        solToken: Token.Active,
        recipientAddress: SearchResult
    ) {
        this.recipientAddress = recipientAddress
        this.solToken = solToken

        onFeeLoading?.invoke(FeeLoadingState.Instant(isLoading = true))
        try {
            initializeWithToken(initialToken)
            initializeCompleted = true
        } catch (e: Throwable) {
            initializeCompleted = false
            currentState = Failure(FeesCalculationError)
        } finally {
            onFeeLoading?.invoke(FeeLoadingState.Instant(isLoading = false))
        }
    }

    private suspend fun initializeWithToken(initialToken: Token.Active) {
        minRentExemption = sendInteractor.getMinRelayRentExemption()
        feeLimitInfo = sendInteractor.getFreeTransactionsInfo()
        sendInteractor.initialize(initialToken)
    }

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
        strategy: FeePayerSelectionStrategy,
        tokenAmount: BigDecimal,
        useCache: Boolean
    ) {
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
                }
                is FeeCalculationState.PoolsNotFound -> {
                    val solanaFee = buildSolanaFee(solToken, sourceToken, feeState.feeInSol)
                    currentState = UpdateFee(solanaFee = solanaFee, feeLimitInfo = feeLimitInfo)
                    sendInteractor.setFeePayerToken(solToken)
                }
                is FeeCalculationState.Success -> {
                    val inputAmount = tokenAmount.toLamports(sourceToken.decimals)
                    showFeeDetails(
                        sourceToken = sourceToken,
                        feeRelayerFee = feeState.fee,
                        feePayerToken = feePayer,
                        inputAmount = inputAmount,
                        strategy = strategy
                    )
                }
                is FeeCalculationState.Error -> {
                    Timber.e(feeState.error, "Error during FeeRelayer fee calculation")
                    handleError(FeesCalculationError)
                }
            }
        } catch (e: CancellationException) {
            Timber.i("Smart selection job was cancelled")
        } catch (e: Throwable) {
            Timber.e(e, "Error during FeeRelayer fee calculation")
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

    private suspend fun showFeeDetails(
        sourceToken: Token.Active,
        feeRelayerFee: FeeRelayerFee,
        feePayerToken: Token.Active,
        inputAmount: BigInteger,
        strategy: FeePayerSelectionStrategy
    ) {
        val fee = buildSolanaFee(feePayerToken, sourceToken, feeRelayerFee)

        if (strategy == FeePayerSelectionStrategy.NO_ACTION) {
            validateFunds(sourceToken, fee, inputAmount)
            currentState = UpdateFee(fee, feeLimitInfo)
        } else {
            validateAndSelectFeePayer(sourceToken, fee, inputAmount, strategy)
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
        feeRelayerFee: FeeRelayerFee
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
            sourceToken = source
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

        /*
         * Checking if fee payer is SOL, otherwise fee payer is already correctly set up
         * - if there is enough SPL balance to cover fee, setting the default fee payer as SPL token
         * - if there is not enough SPL/SOL balance to cover fee, trying to reduce input amount
         * - In other cases, switching to SOL
         * */
        when (val state = fee.calculateFeePayerState(strategy, tokenTotal, inputAmount)) {
            is FeePayerState.SwitchToSpl -> {
                sendInteractor.setFeePayerToken(state.tokenToSwitch)
            }
            is FeePayerState.SwitchToSol -> {
                sendInteractor.switchFeePayerToSol(this.solToken)
            }
            is FeePayerState.ReduceInputAmount -> {
                sendInteractor.setFeePayerToken(sourceToken)
                currentState = ReduceAmount(state.maxAllowedAmount)
            }
        }

        recalculate(sourceToken, inputAmount)
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
            Timber.i("Fee calculation is cancelled")
            null
        } catch (e: Throwable) {
            Timber.e(e, "Error calculating fees")
            handleError(FeesCalculationError)
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
                handleError(FeesCalculationError)
            }
            else -> Unit
        }
    }

    private fun handleError(error: FeeRelayerStateError) {
        currentState = Failure(error)
    }
}

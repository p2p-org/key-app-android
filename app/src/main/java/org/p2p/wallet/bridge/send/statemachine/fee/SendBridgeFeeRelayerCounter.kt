package org.p2p.wallet.bridge.send.statemachine.fee

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.CancellationException
import org.p2p.core.token.Token
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toLamports
import org.p2p.solanaj.core.FeeAmount
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.statemachine.SendFeatureException
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerTopUpInteractor
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePoolsState
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.SendSolanaFee
import org.p2p.wallet.user.interactor.UserInteractor

class SendBridgeFeeRelayerCounter constructor(
    private val userInteractor: UserInteractor,
    private val sendInteractor: SendInteractor,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
) {

    // TODO FIX minRentExemption - need only for validateFunds remove if not needed
    private var minRentExemption: BigInteger = BigInteger.ZERO

    private val alternativeTokensMap: HashMap<String, List<Token.Active>> = HashMap()

    var tokenToPayFee: Token.Active? = null
        private set

    var feeRelayerFee: FeeRelayerFee? = null
        private set

    suspend fun calculateFeeForPayer(
        sourceToken: Token.Active,
        feePayerToken: Token.Active?,
        recipient: String,
        strategy: FeePayerSelectionStrategy,
        tokenAmount: BigDecimal,
        bridgeFees: BridgeSendFees,
    ) {
        val feePayer = feePayerToken ?: tokenToPayFee ?: sourceToken
        // feePayer = sourceToken for first case when tokenToPayFee is not initialised
        val solToken = userInteractor.getUserSolToken() ?: error("Error on getting SOL Token")

        try {
            val feeState = calculateFeesForFeeRelayer(
                feePayerToken = feePayer,
                bridgeFees = bridgeFees,
            )

            suspend fun recalculate() {
                calculateFeeForPayer(
                    sourceToken = sourceToken,
                    feePayerToken = tokenToPayFee,
                    recipient = recipient,
                    strategy = strategy,
                    tokenAmount = tokenAmount,
                    bridgeFees = bridgeFees,
                )
            }

            when (feeState) {
                is FeeCalculationState.NoFees -> {
                    Timber.d("All is Ok just NoFees")
                    tokenToPayFee = sourceToken
                    feeRelayerFee = null
                }
                is FeeCalculationState.PoolsNotFound -> {
                    Timber.d("Error during FeeRelayer PoolsNotFound")
                    tokenToPayFee = solToken
                    feeRelayerFee = feeState.feeInSol
                    recalculate()
                }
                is FeeCalculationState.Success -> {
                    tokenToPayFee = sourceToken
                    feeRelayerFee = feeState.fee
                    val inputAmount = tokenAmount.toLamports(sourceToken.decimals)
                    val fee = buildSolanaFee(feePayer, sourceToken, feeState.fee)
                    validateAndSelectFeePayer(
                        solToken = solToken,
                        sourceToken = sourceToken,
                        feePayerToken = feePayer,
                        fee = fee,
                        inputAmount = inputAmount,
                        strategy = strategy
                    ) { recalculate() }
                }
                is FeeCalculationState.Error -> {
                    Timber.e(feeState.error, "Error during FeePayer fee calculation")
                    throw SendFeatureException.FeeLoadingError(feeState.error.message)
                }
            }
        } catch (e: CancellationException) {
            Timber.i("Smart selection job was cancelled")
        } catch (e: Throwable) {
            Timber.e(e, "Error during FeeRelayer fee calculation")
        }
    }

    private suspend fun validateAndSelectFeePayer(
        solToken: Token.Active,
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        fee: SendSolanaFee,
        inputAmount: BigInteger,
        strategy: FeePayerSelectionStrategy,
        recalculateBlock: suspend () -> Unit
    ) {

        // Assuming token is not SOL
        val tokenTotal = sourceToken.total.toLamports(sourceToken.decimals)

        /*
         * Checking if fee payer is SOL, otherwise fee payer is already correctly set up
         * - if there is enough SPL balance to cover fee, setting the default fee payer as SPL token
         * - if there is not enough SPL/SOL balance to cover fee, trying to reduce input amount
         * - In other cases, switching to SOL
         * */
        val prevFeePayerMint = feePayerToken.mintAddress
        when (val state = fee.calculateFeePayerState(strategy, tokenTotal, inputAmount)) {
            is FeePayerState.SwitchToSpl -> {
                tokenToPayFee = state.tokenToSwitch
            }
            is FeePayerState.SwitchToSol -> {
                tokenToPayFee = solToken
            }
            is FeePayerState.ReduceInputAmount -> {
                tokenToPayFee = sourceToken
                // currentState = FeeRelayerState.ReduceAmount(state.maxAllowedAmount) todo remove or use ReduceAmount
            }
        }

        if (prevFeePayerMint != tokenToPayFee?.mintAddress) {
            recalculateBlock.invoke()
        }
    }

    private suspend fun calculateFeesForFeeRelayer(
        feePayerToken: Token.Active,
        bridgeFees: BridgeSendFees,
    ): FeeCalculationState {
        try {
            val expectedFee = FeeAmount(
                transaction = bridgeFees.networkFee.amount?.toBigIntegerOrNull().orZero() +
                    bridgeFees.bridgeFee.amount?.toBigIntegerOrNull().orZero(),
                accountBalances = bridgeFees.messageAccountRent.amount?.toBigIntegerOrNull().orZero()
            )

            val fees = feeRelayerTopUpInteractor.calculateNeededTopUpAmount(expectedFee)

            if (fees.total.isZero()) {
                return FeeCalculationState.NoFees
            }

            val poolsStateFee = getFeesInPayingToken(
                feePayerToken = feePayerToken,
                transactionFeeInSOL = fees.transaction,
                accountCreationFeeInSOL = fees.accountBalances
            )

            return when (poolsStateFee) {
                is FeePoolsState.Calculated -> {
                    FeeCalculationState.Success(FeeRelayerFee(fees, poolsStateFee.feeInSpl, expectedFee))
                }
                is FeePoolsState.Failed -> {
                    FeeCalculationState.PoolsNotFound(FeeRelayerFee(fees, poolsStateFee.feeInSOL, expectedFee))
                }
            }
        } catch (e: Throwable) {
            return FeeCalculationState.Error(e)
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
            solToken = userInteractor.getUserSolToken(),
            feeRelayerFee = feeRelayerFee,
            alternativeFeePayerTokens = alternativeTokens,
            sourceToken = source
        )
    }

    private fun validateFunds(source: Token.Active, fee: SendSolanaFee, inputAmount: BigInteger): Boolean {
        return fee.isEnoughToCoverExpenses(
            sourceTokenTotal = source.totalInLamports,
            inputAmount = inputAmount,
            minRentExemption = minRentExemption
        )
    }

    private suspend fun getFeesInPayingToken(
        feePayerToken: Token.Active,
        transactionFeeInSOL: BigInteger,
        accountCreationFeeInSOL: BigInteger
    ): FeePoolsState {
        if (feePayerToken.isSOL) {
            val fee = FeeAmount(
                transaction = transactionFeeInSOL,
                accountBalances = accountCreationFeeInSOL
            )
            return FeePoolsState.Calculated(fee)
        }

        return feeRelayerInteractor.calculateFeeInPayingToken(
            feeInSOL = FeeAmount(transaction = transactionFeeInSOL, accountBalances = accountCreationFeeInSOL),
            payingFeeTokenMint = feePayerToken.mintAddress
        )
    }
}

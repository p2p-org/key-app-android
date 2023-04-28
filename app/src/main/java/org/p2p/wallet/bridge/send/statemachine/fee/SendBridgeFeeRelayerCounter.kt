package org.p2p.wallet.bridge.send.statemachine.fee

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toLamports
import org.p2p.solanaj.core.FeeAmount
import org.p2p.wallet.bridge.send.interactor.BridgeSendInteractor
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
    private val bridgeSendInteractor: BridgeSendInteractor,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
) {

    // TODO FIX minRentExemption - need only for validateFunds remove if not needed
    private var minRentExemption: BigInteger = BigInteger.ZERO

    private val supportedPayerTokensMap: HashMap<String, List<Token.Active>> = HashMap()

    suspend fun calculateFeeForPayer(
        sourceToken: Token.Active,
        feePayerToken: Token.Active?,
        tokenAmount: BigDecimal,
        bridgeFees: BridgeSendFees,
    ): Pair<Token.Active, FeeRelayerFee?> {
        val feePayer = feePayerToken ?: sourceToken
        // feePayer = sourceToken for first case when tokenToPayFee is not initialised
        val solToken = userInteractor.getUserSolToken() ?: error("Error on getting SOL Token")

        val (tokenToPayFee, feeState) = calculateFeesForFeeRelayer(
            tokenAmount = tokenAmount,
            sourceToken = sourceToken,
            feePayerToken = feePayer,
            solToken = solToken,
            bridgeFees = bridgeFees,
        )
        Timber.tag("FeePayer").d("${tokenToPayFee.tokenSymbol}: ${tokenToPayFee.mintAddress}")

        return when (feeState) {
            is FeeCalculationState.NoFees -> {
                Timber.d("All is Ok just NoFees")
                tokenToPayFee to null
            }
            is FeeCalculationState.PoolsNotFound -> {
                Timber.tag("FeePayer").d("${feeState.feeInSol}")
                tokenToPayFee to feeState.feeInSol
            }
            is FeeCalculationState.Success -> {
                Timber.tag("FeePayer").d("${feeState.fee}")
                tokenToPayFee to feeState.fee
            }
            is FeeCalculationState.Error -> {
                Timber.e(feeState.error, "Error during FeePayer fee calculation")
                throw SendFeatureException.FeeLoadingError(feeState.error.message)
            }
            is FeeCalculationState.Cancelled -> {
                Timber.d("FeePayer fee calculation cancelled")
                tokenToPayFee to null
            }
        }
    }

    private suspend fun calculateFeesForFeeRelayer(
        tokenAmount: BigDecimal,
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        solToken: Token.Active,
        bridgeFees: BridgeSendFees,
    ): Pair<Token.Active, FeeCalculationState> {
        try {
            val expectedFee = FeeAmount(
                transaction = bridgeFees.networkFee.amount?.toBigIntegerOrNull().orZero() +
                    bridgeFees.bridgeFee.amount?.toBigIntegerOrNull().orZero(),
                accountBalances = bridgeFees.messageAccountRent.amount?.toBigIntegerOrNull().orZero()
            )

            val fees = feeRelayerTopUpInteractor.calculateNeededTopUpAmount(expectedFee)

            if (fees.total.isZero()) {
                return feePayerToken to FeeCalculationState.NoFees
            }

            val poolsStateFee = getFeesInPayingToken(
                feePayerToken = feePayerToken,
                transactionFeeInSOL = fees.transaction,
                accountCreationFeeInSOL = fees.accountBalances
            )

            return when (poolsStateFee) {
                is FeePoolsState.Calculated -> {
                    val feePayerFee = FeeRelayerFee(fees, poolsStateFee.feeInSpl, expectedFee)
                    val fee = buildSolanaFee(feePayerToken, sourceToken, feePayerFee)
                    if (feePayerToken.isSOL) {
                        if (fee.isEnoughSolBalance()) {
                            feePayerToken to FeeCalculationState.Success(feePayerFee)
                        } else {
                            Timber.d("We can pay only in SOL but we have Insufficient Funds to cover this")
                            throw SendFeatureException.InsufficientFunds(tokenAmount)
                        }
                    } else {
                        Timber.tag("FeePayer").d("Payer candidate found ${feePayerToken.tokenSymbol}")
                        // Assuming token is not SOL
                        val tokenTotal = sourceToken.total.toLamports(sourceToken.decimals)
                        val inputAmount = tokenAmount.toLamports(sourceToken.decimals)
                        val newFeePayer = when (
                            val state = fee.calculateFeePayerState(
                                strategy = FeePayerSelectionStrategy.NO_ACTION,
                                sourceTokenTotal = tokenTotal,
                                inputAmount = inputAmount
                            )
                        ) {
                            is FeePayerState.SwitchToSpl -> {
                                state.tokenToSwitch
                            }
                            is FeePayerState.SwitchToSol -> {
                                solToken
                            }
                            is FeePayerState.ReduceInputAmount -> {
                                // currentState = FeeRelayerState.ReduceAmount(state.maxAllowedAmount) todo remove or use ReduceAmount
                                sourceToken
                            }
                        }
                        if (feePayerToken.mintAddress != newFeePayer.mintAddress) {
                            return calculateFeesForFeeRelayer(
                                tokenAmount = tokenAmount,
                                sourceToken = sourceToken,
                                feePayerToken = newFeePayer,
                                bridgeFees = bridgeFees,
                                solToken = solToken,
                            )
                        } else {
                            feePayerToken to FeeCalculationState.Success(feePayerFee)
                        }
                    }
                }
                is FeePoolsState.Failed -> {
                    val feePayerFee = FeeRelayerFee(fees, poolsStateFee.feeInSOL, expectedFee)
                    val fee = buildSolanaFee(feePayerToken, sourceToken, feePayerFee)
                    val alternativeToken = fee.firstAlternativeTokenOrNull()
                    if (alternativeToken == null) {
                        feePayerToken to FeeCalculationState.PoolsNotFound(feePayerFee)
                    } else {
                        calculateFeesForFeeRelayer(
                            tokenAmount = tokenAmount,
                            sourceToken = sourceToken,
                            feePayerToken = alternativeToken,
                            bridgeFees = bridgeFees,
                            solToken = solToken,
                        )
                    }
                }
            }
        } catch (e: Throwable) {
            return feePayerToken to FeeCalculationState.Error(e)
        }
    }

    private suspend fun buildSolanaFee(
        newFeePayer: Token.Active,
        source: Token.Active,
        feeRelayerFee: FeeRelayerFee
    ): SendSolanaFee {
        val keyForAlternativeRequest = "${source.tokenSymbol}_${feeRelayerFee.totalInSol}"
        var supportedPayerTokens = supportedPayerTokensMap[keyForAlternativeRequest]
        if (supportedPayerTokens == null) {
            supportedPayerTokens = sendInteractor.findSupportedFeePayerTokens(
                userTokens = bridgeSendInteractor.supportedSendTokens(),
                transactionFeeInSOL = feeRelayerFee.transactionFeeInSol,
                accountCreationFeeInSOL = feeRelayerFee.accountCreationFeeInSol
            )
            supportedPayerTokensMap[keyForAlternativeRequest] = supportedPayerTokens
        }
        val alternativeTokens = supportedPayerTokens.filter { it.tokenSymbol != newFeePayer.tokenSymbol }
        return SendSolanaFee(
            feePayerToken = newFeePayer,
            solToken = userInteractor.getUserSolToken(),
            feeRelayerFee = feeRelayerFee,
            sourceToken = source,
            alternativeFeePayerTokens = alternativeTokens,
            supportedFeePayerTokens = supportedPayerTokens,
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

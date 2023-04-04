package org.p2p.wallet.bridge.send.statemachine.fee

import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.p2p.core.token.SolAddress
import org.p2p.core.token.Token
import org.p2p.core.utils.isNullOrZero
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toLamports
import org.p2p.solanaj.core.FeeAmount
import org.p2p.wallet.bridge.model.BridgeAmount
import org.p2p.wallet.bridge.model.BridgeFee
import org.p2p.wallet.bridge.send.interactor.EthereumSendInteractor
import org.p2p.wallet.bridge.send.model.BridgeSendFees
import org.p2p.wallet.bridge.send.statemachine.SendFeatureException
import org.p2p.wallet.bridge.send.statemachine.SendState
import org.p2p.wallet.bridge.send.statemachine.bridgeFee
import org.p2p.wallet.bridge.send.statemachine.bridgeToken
import org.p2p.wallet.bridge.send.statemachine.inputAmount
import org.p2p.wallet.bridge.send.statemachine.mapper.SendBridgeStaticStateMapper
import org.p2p.wallet.bridge.send.statemachine.model.SendFee
import org.p2p.wallet.bridge.send.statemachine.model.SendInitialData
import org.p2p.wallet.bridge.send.statemachine.model.SendToken
import org.p2p.wallet.bridge.send.statemachine.validator.SendBridgeValidator
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerTopUpInteractor
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeePayerSelectionStrategy
import org.p2p.wallet.feerelayer.model.FeePoolsState
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.newsend.interactor.SendInteractor
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.SendSolanaFee
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.user.interactor.UserInteractor

class SendBridgeFeeLoader constructor(
    private val initialData: SendInitialData.Bridge,
    private val mapper: SendBridgeStaticStateMapper,
    private val validator: SendBridgeValidator,
    private val ethereumSendInteractor: EthereumSendInteractor,
    private val userInteractor: UserInteractor,
    private val sendInteractor: SendInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
    private val addressInteractor: TransactionAddressInteractor,
) {

    // TODO FIX minRentExemption - need only for validateFunds remove if not needed
    private var minRentExemption: BigInteger = BigInteger.ZERO
    private var freeTransactionFeeLimit: FreeTransactionFeeLimit? = null
    private val alternativeTokensMap: HashMap<String, List<Token.Active>> = HashMap()
    private lateinit var tokenToPayFee: Token.Active
    private var feeRelayerFee: FeeRelayerFee? = null

    fun updateFeeIfNeed(
        lastStaticState: SendState.Static
    ): Flow<SendState> = flow {

        val token = lastStaticState.bridgeToken ?: return@flow
        val oldFee = lastStaticState.bridgeFee

        val isNeedRefresh = !validator.isFeeValid(oldFee)

        if (isNeedRefresh) {
            emit(SendState.Loading.Fee(lastStaticState))
            val fee = loadFee(token, lastStaticState.inputAmount.orZero())
            emit(mapper.updateFee(lastStaticState, fee))
        }
    }

    fun updateFee(
        lastStaticState: SendState.Static
    ): Flow<SendState> = flow {

        val token = lastStaticState.bridgeToken ?: return@flow

        emit(SendState.Loading.Fee(lastStaticState))
        val fee = loadFee(token, lastStaticState.inputAmount.orZero())
        emit(mapper.updateFee(lastStaticState, fee))
    }

    private suspend fun loadFee(
        bridgeToken: SendToken.Bridge,
        amount: BigDecimal,
    ): SendFee.Bridge {
        return try {
            val token = bridgeToken.token
            if (!::tokenToPayFee.isInitialized) {
                tokenToPayFee = token
            }

            val sendTokenMint = if (token.isSOL) {
                null
            } else {
                SolAddress(token.mintAddress)
            }

            val formattedAmount = amount.toLamports(token.decimals)

            if (freeTransactionFeeLimit == null) {
                freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
            }

            val fee = ethereumSendInteractor.getSendFee(
                sendTokenMint = sendTokenMint,
                amount = formattedAmount.toString()
            )

            calculateFeeForPayer(
                sourceToken = token,
                feePayerToken = tokenToPayFee,
                recipient = initialData.recipient.hex,
                strategy = FeePayerSelectionStrategy.SELECT_FEE_PAYER,
                tokenAmount = amount,
                bridgeFees = fee,
            )

            SendFee.Bridge(fee, tokenToPayFee, feeRelayerFee, freeTransactionFeeLimit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw SendFeatureException.FeeLoadingError(e.message)
        }
    }

    private suspend fun calculateFeeForPayer(
        sourceToken: Token.Active,
        feePayerToken: Token.Active?,
        recipient: String,
        strategy: FeePayerSelectionStrategy,
        tokenAmount: BigDecimal,
        bridgeFees: BridgeSendFees,
    ) {
        val feePayer = feePayerToken ?: tokenToPayFee
        val solToken = userInteractor.getUserSolToken()!!

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
                tokenToPayFee = userInteractor.getUserSolToken()!!
            }
            is FeePayerState.ReduceInputAmount -> {
                tokenToPayFee = sourceToken
                // currentState = FeeRelayerState.ReduceAmount(state.maxAllowedAmount) tod
            }
        }

        if (prevFeePayerMint != tokenToPayFee.mintAddress) {
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

    private fun BridgeFee?.toBridgeAmount(): BridgeAmount {
        return BridgeAmount(
            tokenSymbol = this?.symbol.orEmpty(),
            tokenDecimals = this?.decimals.orZero(),
            tokenAmount = this?.amountInToken?.takeIf { !it.isNullOrZero() },
            fiatAmount = this?.amountInUsd?.toBigDecimalOrZero()
        )
    }
}

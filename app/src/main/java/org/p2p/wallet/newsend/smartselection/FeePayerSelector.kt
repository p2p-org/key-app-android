package org.p2p.wallet.newsend.smartselection

import java.math.BigInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.token.Token
import org.p2p.core.utils.isLessThan
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.model.SendSolanaFee

class FeePayerSelector(
    private val sourceSolValidator: SourceSolTokenValidator,
    private val sourceSplValidator: SourceSplTokenValidator,
    private val solValidator: SolTokenValidator,
    private val splValidator: SplTokenValidator,
    private val amountValidator: AmountReducerValidator
) {

    private val feePayerState: MutableStateFlow<FeePayerState> = MutableStateFlow(FeePayerState.SwitchToSol)

    fun getFeePayerStateFlow(): Flow<FeePayerState> = feePayerState

    fun execute(
        sourceToken: Token.Active,
        fee: SendSolanaFee,
        inputAmount: BigInteger,
        isCorrectableAmount: Boolean
    ) {
        val feeRelayerFee = fee.feeRelayerFee
        val feePayerToken = fee.feePayerToken

        when {
            sourceSolValidator.isSolAvailable(inputAmount, sourceToken, feeRelayerFee) -> {
                updateState(FeePayerState.SwitchToSol)
            }

            sourceSplValidator.isSplAvailable(inputAmount, sourceToken, feeRelayerFee, feePayerToken) -> {
                updateState(FeePayerState.SwitchToSpl(sourceToken))
            }

            solValidator.isSolAvailable(sourceToken, feeRelayerFee, feePayerToken) -> {
                updateState(FeePayerState.SwitchToSol)
            }

            splValidator.isSplAvailable(sourceToken, feeRelayerFee, feePayerToken) -> {
                updateState(FeePayerState.SwitchToSpl(feePayerToken))
            }

            isCorrectableAmount && amountValidator.isValid(inputAmount, sourceToken, feeRelayerFee, feePayerToken) -> {
                val totalNeeded = feeRelayerFee.totalInSpl + inputAmount
                val diff = totalNeeded - sourceToken.totalInLamports
                val desiredAmount = if (diff.isLessThan(inputAmount)) inputAmount - diff else null
                val newState = if (desiredAmount != null) {
                    FeePayerState.ReduceInputAmount(fee, sourceToken, desiredAmount)
                } else {
                    FeePayerState.SwitchToSol
                }
                updateState(newState)
            }

            else -> {
                updateState(FeePayerState.SwitchToSol)
            }
        }
    }

    private fun updateState(newState: FeePayerState) {
        feePayerState.value = newState
    }

//    val keyForAlternativeRequest = "${source.tokenSymbol}_${feeRelayerFee.totalInSol}"
//    var alternativeTokens = alternativeTokensMap[keyForAlternativeRequest]
//    if (alternativeTokens == null) {
//        alternativeTokens = sendInteractor.findAlternativeFeePayerTokens(
//            userTokens = userInteractor.getNonZeroUserTokens(),
//            feePayerToExclude = newFeePayer,
//            transactionFeeInSOL = feeRelayerFee.transactionFeeInSol,
//            accountCreationFeeInSOL = feeRelayerFee.accountCreationFeeInSol
//        )
//        alternativeTokensMap[keyForAlternativeRequest] = alternativeTokens
//    }
}

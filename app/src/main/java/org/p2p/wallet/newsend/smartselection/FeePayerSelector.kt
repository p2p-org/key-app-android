package org.p2p.wallet.newsend.smartselection

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.isLessThan
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.FeePayerState
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy

class FeePayerSelector {

    private val strategies = mutableListOf<FeePayerSelectionStrategy>()

    fun setStrategies(newStrategies: List<FeePayerSelectionStrategy>) {
        strategies.clear()
        strategies += newStrategies
    }

    fun execute(): FeePayerState {
        val state = strategies.firstOrNull { it.execute() }
    }

    fun executeInitialSelection(
        sourceToken: Token.Active,
        feePayerToken: Token.Active
        feeRelayerFee: FeeRelayerFee
    ) {
        when {
            sourceSolValidator.isSolAvailable(BigInteger.ZERO, sourceToken, feeRelayerFee) -> {
                updateState(FeePayerState.SwitchToSol)
            }

            sourceSplValidator.isSplAvailable(BigInteger.ZERO, sourceToken, feeRelayerFee, feePayerToken) -> {
                updateState(FeePayerState.SwitchToSpl(sourceToken))
            }
        }
    }

    fun execute(
        sourceToken: Token.Active,
        feePayerToken: Token.Active
        feeRelayerFee: FeeRelayerFee,
        inputAmount: BigInteger,
        isCorrectableAmount: Boolean
    ) {

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

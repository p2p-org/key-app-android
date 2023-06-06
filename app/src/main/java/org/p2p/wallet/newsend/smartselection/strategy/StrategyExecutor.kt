package org.p2p.wallet.newsend.smartselection.strategy

import timber.log.Timber
import org.p2p.wallet.newsend.model.FeePayerState

class StrategyExecutor {

    companion object {
        private const val TAG = "FeePayerSelector"
    }

    private val strategies = linkedSetOf<FeePayerSelectionStrategy>()

    fun setStrategies(newStrategies: LinkedHashSet<FeePayerSelectionStrategy>) {
        strategies.clear()
        strategies += newStrategies
    }

    fun execute(): FeePayerState {
        if (strategies.isEmpty()) {
            Timber.tag(TAG).d("No strategies are set")
            return FeePayerState.NoStrategiesFound
        }

        val strategy = strategies.firstOrNull { it.isPayable() }
        return strategy?.execute() ?: FeePayerState.NoStrategiesFound
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

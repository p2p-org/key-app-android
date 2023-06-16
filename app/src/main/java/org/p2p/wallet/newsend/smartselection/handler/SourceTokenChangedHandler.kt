package org.p2p.wallet.newsend.smartselection.handler

import org.p2p.core.token.Token
import org.p2p.core.utils.orZero
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.smartselection.FeeCalculator
import org.p2p.wallet.newsend.smartselection.SmartSelectionTrigger
import org.p2p.wallet.newsend.smartselection.strategy.AmountReduceStrategy
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSplTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SplTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.ValidationStrategy

class SourceTokenChangedHandler(
    dispatchers: CoroutineDispatchers,
    private val recipient: SearchResult,
    private val feeCalculator: FeeCalculator
) : SendTriggerHandler(dispatchers, feeCalculator, recipient) {

    override fun canHandle(trigger: SmartSelectionTrigger): Boolean =
        trigger is SmartSelectionTrigger.SourceTokenChanged

    override suspend fun generateFeeStrategies(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active,
        fee: FeeRelayerFee
    ): LinkedHashSet<FeePayerSelectionStrategy> {
        if (trigger !is SmartSelectionTrigger.SourceTokenChanged) return linkedSetOf()

        val sourceToken = trigger.newSourceToken
        val inputAmount = trigger.inputAmount
        val solToken = trigger.solToken

        val alternativeFeePayers = feeCalculator.findSingleFeePayer(fee, sourceToken)
            ?.let { listOf(it) } ?: emptyList()

        return linkedSetOf(
            SourceSplTokenStrategy(sourceToken = sourceToken, inputAmount = inputAmount, fee = fee),
            SourceSolanaTokenStrategy(
                recipient = recipient,
                sourceToken = sourceToken,
                inputAmount = inputAmount,
                fee = fee,
                minRentExemption = feeCalculator.getMinRentExemption()
            ),
            SolanaTokenStrategy(
                solToken = solToken,
                sourceToken = sourceToken,
                inputAmount = inputAmount,
                fee = fee
            ),

            SplTokenStrategy(
                sourceToken = sourceToken,
                feePayerToken = feePayerToken,
                inputAmount = inputAmount,
                fee = fee,
                alternativeTokens = alternativeFeePayers
            ),
            AmountReduceStrategy(
                sourceToken = sourceToken,
                inputAmount = inputAmount,
                fee = fee,
                feePayerToken = feePayerToken
            )
        )
    }

    override suspend fun generateNoFeesStrategies(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ): LinkedHashSet<FeePayerSelectionStrategy> {
        if (trigger !is SmartSelectionTrigger.SourceTokenChanged) return linkedSetOf()
        val minRentExemption = feeCalculator.getMinRentExemption()

        return linkedSetOf(
            SourceSolanaTokenStrategy(
                recipient = recipient,
                sourceToken = trigger.newSourceToken,
                inputAmount = trigger.inputAmount,
                fee = FeeRelayerFee.EMPTY,
                minRentExemption = minRentExemption
            ),
            SourceSplTokenStrategy(
                sourceToken = trigger.newSourceToken,
                inputAmount = trigger.inputAmount,
                fee = FeeRelayerFee.EMPTY
            ),
            ValidationStrategy(
                sourceToken = trigger.newSourceToken,
                feePayerToken = trigger.newSourceToken,
                minRentExemption = minRentExemption,
                inputAmount = trigger.inputAmount.orZero(),
                fee = FeeRelayerFee.EMPTY
            )
        )
    }
}

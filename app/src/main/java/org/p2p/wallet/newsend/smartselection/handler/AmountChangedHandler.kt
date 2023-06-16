package org.p2p.wallet.newsend.smartselection.handler

import org.p2p.core.token.Token
import org.p2p.core.utils.orZero
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.smartselection.FeeCalculator
import org.p2p.wallet.newsend.smartselection.SmartSelectionTrigger
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSplTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SplTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.ValidationStrategy

class AmountChangedHandler(
    dispatchers: CoroutineDispatchers,
    private val feeCalculator: FeeCalculator,
    private val recipient: SearchResult
) : SendTriggerHandler(dispatchers, feeCalculator, recipient) {

    override fun canHandle(trigger: SmartSelectionTrigger): Boolean =
        trigger is SmartSelectionTrigger.AmountChanged

    override suspend fun generateFeeStrategies(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active,
        fee: FeeRelayerFee
    ): LinkedHashSet<FeePayerSelectionStrategy> {
        if (trigger !is SmartSelectionTrigger.AmountChanged) return linkedSetOf()

        val sourceToken = trigger.sourceToken
        val solToken = trigger.solToken
        val inputAmount = trigger.inputAmount
        val minRentExemption = feeCalculator.getMinRentExemption()

        val alternativeFeePayers = feeCalculator.findSingleFeePayer(fee, sourceToken)
            ?.let { listOf(it) } ?: emptyList()

        val strategies = linkedSetOf<FeePayerSelectionStrategy>()

        strategies += SourceSplTokenStrategy(
            sourceToken = sourceToken,
            inputAmount = inputAmount,
            fee = fee
        )

        strategies += SourceSolanaTokenStrategy(
            recipient = recipient,
            sourceToken = sourceToken,
            inputAmount = inputAmount,
            fee = fee,
            minRentExemption = minRentExemption
        )

        strategies += SolanaTokenStrategy(
            solToken = solToken,
            sourceToken = sourceToken,
            inputAmount = inputAmount,
            fee = fee
        )

        strategies += SplTokenStrategy(
            sourceToken = sourceToken,
            feePayerToken = sourceToken,
            inputAmount = inputAmount,
            fee = fee,
            alternativeTokens = alternativeFeePayers
        )

        strategies += ValidationStrategy(
            sourceToken = sourceToken,
            feePayerToken = sourceToken,
            minRentExemption = minRentExemption,
            inputAmount = inputAmount.orZero(),
            fee = fee
        )

        return strategies
    }

    override suspend fun generateNoFeesStrategies(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ): LinkedHashSet<FeePayerSelectionStrategy> {
        if (trigger !is SmartSelectionTrigger.AmountChanged) return linkedSetOf()

        val strategies = linkedSetOf<FeePayerSelectionStrategy>()

        strategies += SourceSolanaTokenStrategy(
            recipient = recipient,
            sourceToken = trigger.sourceToken,
            inputAmount = trigger.inputAmount,
            fee = FeeRelayerFee.EMPTY,
            minRentExemption = feeCalculator.getMinRentExemption()
        )

        strategies += SourceSplTokenStrategy(
            sourceToken = trigger.sourceToken,
            inputAmount = trigger.inputAmount,
            fee = FeeRelayerFee.EMPTY
        )

        strategies += ValidationStrategy(
            sourceToken = trigger.sourceToken,
            feePayerToken = trigger.sourceToken,
            minRentExemption = feeCalculator.getMinRentExemption(),
            inputAmount = trigger.inputAmount.orZero(),
            fee = FeeRelayerFee.EMPTY
        )

        return strategies
    }
}

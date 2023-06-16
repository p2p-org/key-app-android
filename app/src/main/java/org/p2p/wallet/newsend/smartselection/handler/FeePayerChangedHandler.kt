package org.p2p.wallet.newsend.smartselection.handler

import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.smartselection.FeeCalculator
import org.p2p.wallet.newsend.smartselection.SmartSelectionTrigger
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy
import org.p2p.wallet.newsend.smartselection.strategy.ValidationStrategy

class FeePayerChangedHandler(
    dispatchers: CoroutineDispatchers,
    recipient: SearchResult,
    private val feeCalculator: FeeCalculator
) : SendTriggerHandler(dispatchers, feeCalculator, recipient) {

    override fun canHandle(trigger: SmartSelectionTrigger): Boolean =
        trigger is SmartSelectionTrigger.FeePayerManuallyChanged

    override suspend fun generateFeeStrategies(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active,
        fee: FeeRelayerFee
    ): LinkedHashSet<FeePayerSelectionStrategy> {
        if (trigger !is SmartSelectionTrigger.FeePayerManuallyChanged) return linkedSetOf()

        val strategies = linkedSetOf<FeePayerSelectionStrategy>()

        strategies += ValidationStrategy(
            sourceToken = trigger.sourceToken,
            feePayerToken = feePayerToken,
            minRentExemption = feeCalculator.getMinRentExemption(),
            inputAmount = trigger.inputAmount,
            fee = fee
        )

        return strategies
    }

    override suspend fun generateNoFeesStrategies(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ): LinkedHashSet<FeePayerSelectionStrategy> = linkedSetOf()
}

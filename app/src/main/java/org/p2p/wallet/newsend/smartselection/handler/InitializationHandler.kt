package org.p2p.wallet.newsend.smartselection.handler

import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.smartselection.FeeCalculator
import org.p2p.wallet.newsend.smartselection.SmartSelectionTrigger
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSplTokenStrategy

class InitializationHandler(
    dispatchers: CoroutineDispatchers,
    private val recipient: SearchResult,
    private val feeCalculator: FeeCalculator
) : SendTriggerHandler(dispatchers, feeCalculator, recipient) {

    override fun canHandle(trigger: SmartSelectionTrigger): Boolean =
        trigger is SmartSelectionTrigger.Initialization

    override suspend fun generateFeeStrategies(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active,
        fee: FeeRelayerFee
    ): LinkedHashSet<FeePayerSelectionStrategy> {
        if (trigger !is SmartSelectionTrigger.Initialization) return linkedSetOf()

        val initialToken = trigger.initialToken
        val initialAmount = trigger.initialAmount
        return linkedSetOf(
            SourceSplTokenStrategy(
                sourceToken = initialToken,
                inputAmount = initialAmount,
                fee = fee
            ),

            SourceSolanaTokenStrategy(
                recipient = recipient,
                sourceToken = initialToken,
                inputAmount = initialAmount,
                fee = fee,
                minRentExemption = feeCalculator.getMinRentExemption()
            )
        )
    }

    override suspend fun generateNoFeesStrategies(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ): LinkedHashSet<FeePayerSelectionStrategy> = linkedSetOf()
}

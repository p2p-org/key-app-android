package org.p2p.wallet.newsend.smartselection.handler

import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.model.smartselection.SmartSelectionState
import org.p2p.wallet.newsend.smartselection.FeeCalculator
import org.p2p.wallet.newsend.smartselection.SmartSelectionTrigger
import org.p2p.wallet.newsend.smartselection.strategy.AmountReduceStrategy
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SplTokenStrategy

class MaxAmountEnteredHandler(
    private val recipient: SearchResult,
    private val feeCalculator: FeeCalculator
) : TriggerHandler {

    override suspend fun handleTrigger(
        currentState: MutableStateFlow<SmartSelectionState>,
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ) {
        if (trigger !is SmartSelectionTrigger.MaxAmountEntered) return

        fun updateState(newState: SmartSelectionState) {
            currentState.value = newState
        }

        val feeState = feeCalculator.calculateFee(
            sourceToken = trigger.sourceToken,
            feePayerToken = feePayerToken,
            recipient = recipient.addressState.address
        )

        val newState = when (feeState) {
            is FeeCalculationState.Success -> {
                val strategies = generateStrategies(
                    solToken = trigger.solToken,
                    sourceToken = trigger.sourceToken,
                    inputAmount = trigger.inputAmount,
                    feePayerToken = feePayerToken,
                    fee = feeState.fee
                )
                SmartSelectionState.ReadyForSmartSelection(strategies)
            }
            is FeeCalculationState.PoolsNotFound -> SmartSelectionState.SolanaFeeOnly(feeState.feeInSol)
            is FeeCalculationState.NoFees -> SmartSelectionState.NoFees(trigger.sourceToken)
            is FeeCalculationState.Cancelled -> SmartSelectionState.Cancelled
            is FeeCalculationState.Failed -> SmartSelectionState.Failed(feeState.e)
        }

        updateState(newState)
    }

    private fun generateStrategies(
        solToken: Token.Active,
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        inputAmount: BigDecimal,
        fee: FeeRelayerFee
    ): LinkedHashSet<FeePayerSelectionStrategy> {
        return linkedSetOf(
            SourceTokenStrategy(sourceToken, inputAmount, fee),
            SolanaTokenStrategy(solToken, sourceToken, inputAmount, fee),
            SplTokenStrategy(sourceToken, feePayerToken, inputAmount, fee, emptyList()),
            AmountReduceStrategy(sourceToken, inputAmount, fee, feePayerToken)
        )
    }
}

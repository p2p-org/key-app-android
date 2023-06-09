package org.p2p.wallet.newsend.smartselection.handler

import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeCalculationState.Cancelled
import org.p2p.wallet.feerelayer.model.FeeCalculationState.Failed
import org.p2p.wallet.feerelayer.model.FeeCalculationState.NoFees
import org.p2p.wallet.feerelayer.model.FeeCalculationState.PoolsNotFound
import org.p2p.wallet.feerelayer.model.FeeCalculationState.Success
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.model.smartselection.SmartSelectionState
import org.p2p.wallet.newsend.smartselection.FeeCalculator
import org.p2p.wallet.newsend.smartselection.SmartSelectionTrigger
import org.p2p.wallet.newsend.smartselection.strategy.AmountReduceStrategy
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSplTokenStrategy
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
            recipient = recipient.address
        )

        val newState = when (feeState) {
            is Success -> {
                val strategies = generateStrategies(
                    solToken = trigger.solToken,
                    sourceToken = trigger.sourceToken,
                    inputAmount = trigger.inputAmount,
                    feePayerToken = feePayerToken,
                    fee = feeState.fee,
                    minRentExemption = feeCalculator.getMinRentExemption()
                )
                SmartSelectionState.ReadyForSmartSelection(strategies)
            }
            is PoolsNotFound -> SmartSelectionState.SolanaFeeOnly(feeState.feeInSol)
            is NoFees -> SmartSelectionState.NoFees(trigger.sourceToken, trigger.inputAmount)
            is Cancelled -> SmartSelectionState.Cancelled
            is Failed -> SmartSelectionState.Failed(feeState.e)
        }

        updateState(newState)
    }

    private fun generateStrategies(
        solToken: Token.Active,
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        inputAmount: BigDecimal,
        fee: FeeRelayerFee,
        minRentExemption: BigInteger
    ): LinkedHashSet<FeePayerSelectionStrategy> {
        return linkedSetOf(
            SourceSplTokenStrategy(sourceToken, inputAmount, fee),
            SourceSolanaTokenStrategy(recipient, sourceToken, inputAmount, fee, minRentExemption),
            SolanaTokenStrategy(solToken, sourceToken, inputAmount, fee),
            SplTokenStrategy(sourceToken, feePayerToken, inputAmount, fee, emptyList()),
            AmountReduceStrategy(sourceToken, inputAmount, fee, feePayerToken)
        )
    }
}

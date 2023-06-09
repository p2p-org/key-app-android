package org.p2p.wallet.newsend.smartselection.handler

import java.math.BigInteger
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.token.Token
import org.p2p.core.utils.orZero
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
import org.p2p.wallet.newsend.smartselection.strategy.SolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSplTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SplTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.ValidationStrategy

class AmountChangedHandler(
    private val recipient: SearchResult,
    private val feeCalculator: FeeCalculator
) : TriggerHandler {

    override suspend fun handleTrigger(
        currentState: MutableStateFlow<SmartSelectionState>,
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ) {

        if (trigger !is SmartSelectionTrigger.AmountChanged) return

        val minRentExemption = feeCalculator.getMinRentExemption()

        val feeState = feeCalculator.calculateFee(
            sourceToken = trigger.sourceToken,
            feePayerToken = trigger.sourceToken,
            recipient = recipient.address
        )

        val newState = when (feeState) {
            is Success -> generateFeeStrategies(trigger, feeState, minRentExemption)
            is PoolsNotFound -> SmartSelectionState.SolanaFeeOnly(feeState.feeInSol)
            is NoFees -> generateNoFeesStrategies(trigger, minRentExemption)
            is Cancelled -> SmartSelectionState.Cancelled
            is Failed -> SmartSelectionState.Failed(feeState.e)
        }

        currentState.value = newState
    }

    private fun generateFeeStrategies(
        trigger: SmartSelectionTrigger.AmountChanged,
        feeState: Success,
        minRentExemption: BigInteger
    ): SmartSelectionState.ReadyForSmartSelection {
        val sourceToken = trigger.sourceToken
        val solToken = trigger.solToken
        val inputAmount = trigger.inputAmount
        val fee = feeState.fee

        val strategies = linkedSetOf(
            SourceSplTokenStrategy(sourceToken, inputAmount, fee),
            SourceSolanaTokenStrategy(recipient, sourceToken, inputAmount, fee, minRentExemption),
            SolanaTokenStrategy(solToken, sourceToken, inputAmount, fee),
            SplTokenStrategy(sourceToken, sourceToken, inputAmount, fee, emptyList()),
            ValidationStrategy(sourceToken, sourceToken, minRentExemption, inputAmount.orZero(), fee)
        )
        return SmartSelectionState.ReadyForSmartSelection(strategies)
    }

    private fun generateNoFeesStrategies(
        trigger: SmartSelectionTrigger.AmountChanged,
        minRentExemption: BigInteger
    ): SmartSelectionState {
        val strategies = linkedSetOf(
            SourceSolanaTokenStrategy(
                recipient = recipient,
                sourceToken = trigger.sourceToken,
                inputAmount = trigger.inputAmount,
                fee = FeeRelayerFee.EMPTY,
                minRentExemption = minRentExemption
            ),
            SourceSplTokenStrategy(
                sourceToken = trigger.sourceToken,
                inputAmount = trigger.inputAmount,
                fee = FeeRelayerFee.EMPTY
            ),
            ValidationStrategy(
                sourceToken = trigger.sourceToken,
                feePayerToken = trigger.sourceToken,
                minRentExemption = minRentExemption,
                inputAmount = trigger.inputAmount.orZero(),
                fee = FeeRelayerFee.EMPTY
            )
        )

        return SmartSelectionState.ReadyForSmartSelection(strategies)
    }
}

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
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy
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
            feePayerToken = feePayerToken,
            recipient = recipient.addressState.address
        )

        val newState = when (feeState) {
            is Success -> prepareForSmartSelection(trigger, feePayerToken, feeState, minRentExemption)
            is PoolsNotFound -> SmartSelectionState.SolanaFeeOnly(feeState.feeInSol)
            is NoFees -> validateSolOrNoFees(trigger, feePayerToken, minRentExemption)
            is Cancelled -> SmartSelectionState.Cancelled
            is Failed -> SmartSelectionState.Failed(feeState.e)
        }

        currentState.value = newState
    }

    private fun validateSolOrNoFees(
        trigger: SmartSelectionTrigger.AmountChanged,
        feePayerToken: Token.Active,
        minRentExemption: BigInteger
    ) = if (trigger.sourceToken.isSOL) {
        val strategies = generateNoFeesStrategies(trigger, feePayerToken, minRentExemption)
        SmartSelectionState.ReadyForSmartSelection(strategies)
    } else {
        SmartSelectionState.NoFees(trigger.sourceToken, trigger.inputAmount)
    }

    private fun prepareForSmartSelection(
        trigger: SmartSelectionTrigger.AmountChanged,
        feePayerToken: Token.Active,
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
            SolanaTokenStrategy(recipient, solToken, sourceToken, inputAmount, fee, minRentExemption),
            SplTokenStrategy(sourceToken, feePayerToken, inputAmount, fee, emptyList()),
            ValidationStrategy(sourceToken, feePayerToken, minRentExemption, inputAmount.orZero(), fee)
        )
        return SmartSelectionState.ReadyForSmartSelection(strategies)
    }

    private fun generateNoFeesStrategies(
        trigger: SmartSelectionTrigger.AmountChanged,
        feePayerToken: Token.Active,
        minRentExemption: BigInteger
    ): LinkedHashSet<FeePayerSelectionStrategy> {
        return linkedSetOf(
            SolanaTokenStrategy(
                recipient = recipient,
                solToken = trigger.solToken,
                sourceToken = trigger.sourceToken,
                inputAmount = trigger.inputAmount,
                fee = FeeRelayerFee.EMPTY,
                minRentExemption = minRentExemption
            ),
            ValidationStrategy(
                sourceToken = trigger.sourceToken,
                feePayerToken = feePayerToken,
                minRentExemption = minRentExemption,
                inputAmount = trigger.inputAmount.orZero(),
                fee = FeeRelayerFee.EMPTY
            )
        )
    }
}

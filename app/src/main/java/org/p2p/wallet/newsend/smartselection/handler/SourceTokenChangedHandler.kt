package org.p2p.wallet.newsend.smartselection.handler

import java.math.BigInteger
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeCalculationState.Cancelled
import org.p2p.wallet.feerelayer.model.FeeCalculationState.Failed
import org.p2p.wallet.feerelayer.model.FeeCalculationState.NoFees
import org.p2p.wallet.feerelayer.model.FeeCalculationState.PoolsNotFound
import org.p2p.wallet.feerelayer.model.FeeCalculationState.Success
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.model.smartselection.SmartSelectionState
import org.p2p.wallet.newsend.smartselection.FeeCalculator
import org.p2p.wallet.newsend.smartselection.SmartSelectionTrigger
import org.p2p.wallet.newsend.smartselection.strategy.AmountReduceStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSplTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SplTokenStrategy

class SourceTokenChangedHandler(
    private val recipient: SearchResult,
    private val feeCalculator: FeeCalculator
) : TriggerHandler {

    override suspend fun handleTrigger(
        currentState: MutableStateFlow<SmartSelectionState>,
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ) {
        if (trigger !is SmartSelectionTrigger.SourceTokenChanged) return

        fun updateState(newState: SmartSelectionState) {
            currentState.value = newState
        }

        val feeState = feeCalculator.calculateFee(
            sourceToken = trigger.newSourceToken,
            feePayerToken = trigger.newSourceToken,
            recipient = recipient.addressState.address
        )

        val minRentExemption = feeCalculator.getMinRentExemption()

        val newState = when (feeState) {
            is Success -> prepareForSmartSelection(trigger, feePayerToken, feeState, minRentExemption)
            is PoolsNotFound -> SmartSelectionState.SolanaFeeOnly(feeState.feeInSol)
            is NoFees -> SmartSelectionState.NoFees(trigger.newSourceToken, trigger.inputAmount)
            is Cancelled -> SmartSelectionState.Cancelled
            is Failed -> SmartSelectionState.Failed(feeState.e)
        }

        updateState(newState)
    }

    private fun prepareForSmartSelection(
        trigger: SmartSelectionTrigger.SourceTokenChanged,
        feePayerToken: Token.Active,
        feeState: Success,
        minRentExemption: BigInteger
    ): SmartSelectionState.ReadyForSmartSelection {
        val sourceToken = trigger.newSourceToken
        val inputAmount = trigger.inputAmount
        val fee = feeState.fee
        val solToken = trigger.solToken

        val strategies = linkedSetOf(
            SourceSplTokenStrategy(sourceToken, inputAmount, fee),
            SourceSolanaTokenStrategy(recipient, sourceToken, inputAmount, fee, minRentExemption),
            SolanaTokenStrategy(recipient, solToken, sourceToken, inputAmount, fee, minRentExemption),

            // todo: alternative fee payers
            SplTokenStrategy(sourceToken, feePayerToken, inputAmount, fee, emptyList()),
            AmountReduceStrategy(sourceToken, inputAmount, fee, feePayerToken)
        )
        return SmartSelectionState.ReadyForSmartSelection(strategies)
    }
}

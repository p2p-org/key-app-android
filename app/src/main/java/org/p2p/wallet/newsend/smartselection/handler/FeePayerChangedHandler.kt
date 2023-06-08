package org.p2p.wallet.newsend.smartselection.handler

import java.math.BigDecimal
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
import org.p2p.wallet.newsend.smartselection.strategy.ValidationStrategy

class FeePayerChangedHandler(
    private val recipient: SearchResult,
    private val feeCalculator: FeeCalculator
) : TriggerHandler {

    override suspend fun handleTrigger(
        currentState: MutableStateFlow<SmartSelectionState>,
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ) {
        if (trigger !is SmartSelectionTrigger.FeePayerManuallyChanged) return

        val feeState = feeCalculator.calculateFee(
            sourceToken = trigger.sourceToken,
            feePayerToken = trigger.newFeePayer,
            recipient = recipient.addressState.address
        )

        val minRentExemption = feeCalculator.getMinRentExemption()

        val newState = when (feeState) {
            is Success -> {
                val strategies = generateStrategies(
                    sourceToken = trigger.sourceToken,
                    inputAmount = trigger.inputAmount,
                    feePayerToken = feePayerToken,
                    fee = feeState.fee,
                    minRentExemption = minRentExemption
                )
                SmartSelectionState.ReadyForSmartSelection(strategies)
            }
            is PoolsNotFound -> SmartSelectionState.SolanaFeeOnly(feeState.feeInSol)
            is NoFees -> SmartSelectionState.NoFees(trigger.sourceToken, trigger.inputAmount)
            is Cancelled -> SmartSelectionState.Cancelled
            is Failed -> SmartSelectionState.Failed(feeState.e)
        }

        currentState.value = newState
    }

    private fun generateStrategies(
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        inputAmount: BigDecimal,
        fee: FeeRelayerFee,
        minRentExemption: BigInteger
    ): LinkedHashSet<FeePayerSelectionStrategy> {
        return linkedSetOf(
            ValidationStrategy(sourceToken, feePayerToken, minRentExemption, inputAmount.orZero(), fee)
        )
    }
}

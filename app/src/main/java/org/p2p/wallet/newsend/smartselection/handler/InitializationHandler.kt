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
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSolanaTokenStrategy
import org.p2p.wallet.newsend.smartselection.strategy.SourceSplTokenStrategy

class InitializationHandler(
    private val recipient: SearchResult,
    private val feeCalculator: FeeCalculator
) : TriggerHandler {

    override suspend fun handleTrigger(
        currentState: MutableStateFlow<SmartSelectionState>,
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ) {

        if (trigger !is SmartSelectionTrigger.Initialization) return

        val feeState = feeCalculator.calculateFee(
            sourceToken = trigger.initialToken,
            feePayerToken = trigger.initialToken,
            recipient = recipient.addressState.address
        )

        val newState = when (feeState) {
            is Success -> {
                val strategies = generateStrategies(
                    sourceToken = trigger.initialToken,
                    inputAmount = trigger.initialAmount,
                    fee = feeState.fee,
                    minRentExemption = feeCalculator.getMinRentExemption()
                )
                SmartSelectionState.ReadyForSmartSelection(strategies)
            }
            is PoolsNotFound -> {
                SmartSelectionState.SolanaFeeOnly(feeState.feeInSol)
            }
            is NoFees -> {
                SmartSelectionState.NoFees(trigger.initialToken, trigger.initialAmount)
            }
            is Cancelled -> {
                SmartSelectionState.Cancelled
            }
            is Failed -> {
                SmartSelectionState.Failed(feeState.e)
            }
        }

        currentState.value = newState
    }

    private fun generateStrategies(
        minRentExemption: BigInteger,
        sourceToken: Token.Active,
        inputAmount: BigDecimal?,
        fee: FeeRelayerFee
    ): LinkedHashSet<FeePayerSelectionStrategy> {
        return linkedSetOf(
            SourceSplTokenStrategy(sourceToken, inputAmount, fee),
            SourceSolanaTokenStrategy(recipient, sourceToken, inputAmount, fee, minRentExemption)
        )
    }
}

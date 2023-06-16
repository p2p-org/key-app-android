package org.p2p.wallet.newsend.smartselection.handler

import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.newsend.model.SearchResult
import org.p2p.wallet.newsend.model.smartselection.SmartSelectionState
import org.p2p.wallet.newsend.smartselection.FeeCalculator
import org.p2p.wallet.newsend.smartselection.SmartSelectionTrigger
import org.p2p.wallet.newsend.smartselection.getNoFeesData
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy

abstract class SendTriggerHandler(
    private val dispatchers: CoroutineDispatchers,
    private val feeCalculator: FeeCalculator,
    private val recipient: SearchResult
) {

    abstract fun canHandle(trigger: SmartSelectionTrigger): Boolean

    abstract suspend fun generateFeeStrategies(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active,
        fee: FeeRelayerFee
    ): LinkedHashSet<FeePayerSelectionStrategy>

    abstract suspend fun generateNoFeesStrategies(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ): LinkedHashSet<FeePayerSelectionStrategy>

    open suspend fun handleTrigger(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ): SmartSelectionState = withContext(dispatchers.io) {

        return@withContext when (val feeState = calculateFee(trigger, feePayerToken)) {
            is FeeCalculationState.Success -> handleSuccess(trigger, feePayerToken, feeState)
            is FeeCalculationState.PoolsNotFound -> handlePoolsNotFound(feeState)
            is FeeCalculationState.NoFees -> handleNoFees(trigger, feePayerToken)
            is FeeCalculationState.Cancelled -> SmartSelectionState.Cancelled
            is FeeCalculationState.Failed -> SmartSelectionState.Failed(feeState.e)
        }
    }

    private suspend fun handleSuccess(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active,
        feeState: FeeCalculationState.Success
    ): SmartSelectionState.ReadyForSmartSelection {
        val strategies = generateFeeStrategies(
            trigger = trigger,
            feePayerToken = feePayerToken,
            fee = feeState.fee
        )
        return SmartSelectionState.ReadyForSmartSelection(strategies)
    }

    private suspend fun handleNoFees(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ): SmartSelectionState {
        val strategies = generateNoFeesStrategies(
            trigger = trigger,
            feePayerToken = feePayerToken
        )
        return if (strategies.isNotEmpty()) {
            SmartSelectionState.ReadyForSmartSelection(strategies)
        } else {
            val noFeesData = trigger.getNoFeesData()
            SmartSelectionState.NoFees(
                sourceToken = noFeesData.sourceToken,
                initialAmount = noFeesData.initialAmount
            )
        }
    }

    // FIXME: Add switching to SOL
    private fun handlePoolsNotFound(feeState: FeeCalculationState.PoolsNotFound) =
        SmartSelectionState.SolanaFeeOnly(feeState.feeInSol)

    private suspend fun calculateFee(
        trigger: SmartSelectionTrigger,
        feePayerToken: Token.Active
    ): FeeCalculationState {
        val (sourceToken, feePayer) = when (trigger) {
            is SmartSelectionTrigger.Initialization -> trigger.initialToken to trigger.initialToken
            is SmartSelectionTrigger.AmountChanged -> trigger.sourceToken to trigger.sourceToken
            is SmartSelectionTrigger.SourceTokenChanged -> trigger.newSourceToken to trigger.newSourceToken
            is SmartSelectionTrigger.FeePayerManuallyChanged -> trigger.sourceToken to trigger.newFeePayer
            is SmartSelectionTrigger.MaxAmountEntered -> trigger.sourceToken to feePayerToken
        }

        return feeCalculator.calculateFee(
            sourceToken = sourceToken,
            feePayerToken = feePayer,
            recipient = recipient.address
        )
    }
}

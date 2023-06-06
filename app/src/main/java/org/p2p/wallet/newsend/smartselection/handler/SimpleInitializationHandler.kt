package org.p2p.wallet.newsend.smartselection.handler

import timber.log.Timber
import java.util.concurrent.CancellationException
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.newsend.model.smartselection.SmartSelectionState
import org.p2p.wallet.newsend.smartselection.FeeCalculator
import org.p2p.wallet.newsend.smartselection.FeePayerSelector
import org.p2p.wallet.newsend.smartselection.UserSendAction

private const val TAG = "InitializationHandler"

class SimpleInitializationHandler(
    private val feePayerSelector: FeePayerSelector,
    private val feeCalculator: FeeCalculator
) {

    suspend fun handleAction(action: UserSendAction.SimpleInitialization) {
        try {
            val feeInSol = feeCalculator.calculateFeeInSol(
                feePayerToken = action.defaultToken,
                token = action.defaultToken,
                recipient = action.recipient,
                useCache = false
            )

            if (feeInSol.isFree) {
                updateState(SmartSelectionState.NoFees)
                return
            }

            val feeInSpl = feeCalculator.calculateFeeInSpl(
                feePayerToken = validatedAction.defaultToken,
                feeInSol = feeInSol
            )

            val newState = when (feeInSpl) {
                is FeeCalculationState.Success -> SmartSelectionState.FeeCalculated(feeInSpl.fee)
                is FeeCalculationState.PoolsNotFound -> SmartSelectionState.SolFeeCalculated(feeInSpl.feeInSol)
            }

            updateState(newState)
        } catch (e: CancellationException) {
            Timber.tag(TAG).i("Fee calculation cancelled")
        } catch (e: Throwable) {
            Timber.tag(TAG).i(e, "Failed to calculate fee")
            updateState(SmartSelectionState.Failed(e))
        }
    }
}

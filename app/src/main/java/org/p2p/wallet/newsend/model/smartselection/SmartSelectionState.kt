package org.p2p.wallet.newsend.model.smartselection

import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.newsend.smartselection.strategy.FeePayerSelectionStrategy

sealed interface SmartSelectionState {

    object Idle : SmartSelectionState
    data class FeeCalculated(val fee: FeeRelayerFee) : SmartSelectionState
    data class SolFeeCalculated(val feeInSol: FeeRelayerFee) : SmartSelectionState
    object NoFees : SmartSelectionState
    data class Failed(val e: Throwable) : SmartSelectionState
    data class ReadyForSmartSelection(val strategies: List<FeePayerSelectionStrategy>) : SmartSelectionState
}

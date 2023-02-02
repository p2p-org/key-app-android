package org.p2p.wallet.newsend.model

import org.p2p.wallet.feerelayer.model.FeeRelayerFee

sealed interface FeeCalculationState {
    data class FeePayerFound(val splFee: FeeRelayerFee?) : FeeCalculationState
    data class SwitchToSol(val solFee: FeeRelayerFee?) : FeeCalculationState
}

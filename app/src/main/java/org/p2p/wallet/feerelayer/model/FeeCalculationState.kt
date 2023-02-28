package org.p2p.wallet.feerelayer.model

sealed interface FeeCalculationState {
    data class Success(val fee: FeeRelayerFee) : FeeCalculationState
    data class PoolsNotFound(val feeInSol: FeeRelayerFee) : FeeCalculationState
    object NoFees : FeeCalculationState
    data class Error(val error: Throwable) : FeeCalculationState
}

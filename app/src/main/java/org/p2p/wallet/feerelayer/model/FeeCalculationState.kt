package org.p2p.wallet.feerelayer.model

import org.p2p.solanaj.kits.AccountInfoTokenExtensionConfig

sealed interface FeeCalculationState {
    data class Success(
        val fee: FeeRelayerFee,
        val tokenExtensions: Map<String, AccountInfoTokenExtensionConfig>
    ) : FeeCalculationState
    data class PoolsNotFound(val feeInSol: FeeRelayerFee) : FeeCalculationState
    object NoFees : FeeCalculationState
    object Cancelled : FeeCalculationState
    data class Error(val error: Throwable) : FeeCalculationState
}

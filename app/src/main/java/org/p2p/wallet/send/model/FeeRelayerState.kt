package org.p2p.wallet.send.model

import java.math.BigInteger
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits

sealed interface FeeRelayerState {
    object Idle : FeeRelayerState

    data class CalculationUpdate(val state: CalculationState) : FeeRelayerState



    data class ReduceAmount(
        val fee: SendSolanaFee,
        val newInputAmount: BigInteger
    ) : FeeRelayerState

    data class UpdateFee(
        val solanaFee: SendSolanaFee?,
        val feeLimitInfo: TransactionFeeLimits
    ) : FeeRelayerState

    data class Loading(val loadingState: FeeLoadingState) : FeeRelayerState

    object Cancelled : FeeRelayerState

    data class FeeError(val cause: Throwable) : FeeRelayerState

    object InsufficientFundsError : FeeRelayerState

    fun isValidState(): Boolean = this is UpdateFee || this is ReduceAmount || this is Idle
}

fun FeeRelayerState.getFee(): SendSolanaFee? {
    return when (this) {
        is FeeRelayerState.UpdateFee -> solanaFee
        is FeeRelayerState.ReduceAmount -> fee
        else -> null
    }
}

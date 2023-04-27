package org.p2p.wallet.newsend.model

import java.math.BigInteger
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits

sealed interface FeeRelayerState {
    object Idle : FeeRelayerState
    data class ReduceAmount(val newInputAmount: BigInteger) : FeeRelayerState
    data class UpdateFee(
        val solanaFee: SendSolanaFee?,
        val feeLimitInfo: TransactionFeeLimits
    ) : FeeRelayerState
    data class Failure(val errorStateError: FeeRelayerStateError) : FeeRelayerState, Throwable() {
        fun isFeeCalculationError(): Boolean {
            return errorStateError == FeeRelayerStateError.FeesCalculationError
        }
    }

    fun isValidState(): Boolean = this is UpdateFee || this is ReduceAmount || this is Idle
}

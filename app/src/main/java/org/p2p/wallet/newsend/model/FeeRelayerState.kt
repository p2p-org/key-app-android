package org.p2p.wallet.newsend.model

import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import java.math.BigInteger

sealed interface FeeRelayerState {
    object Idle : FeeRelayerState
    data class ReduceAmount(val newInputAmount: BigInteger) : FeeRelayerState
    data class UpdateFee(
        val solanaFee: SendSolanaFee?,
        val feeLimitInfo: FreeTransactionFeeLimit
    ) : FeeRelayerState
    data class Failure(val errorStateError: FeeRelayerStateError) : FeeRelayerState {
        fun isFeeCalculationError(): Boolean {
            return errorStateError == FeeRelayerStateError.FeesCalculationError
        }
    }

    fun isValidState(): Boolean = this is UpdateFee || this is ReduceAmount || this is Idle
}

package org.p2p.wallet.newsend.model

import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.send.model.SendSolanaFee
import java.math.BigInteger

sealed interface FeeRelayerState {
    object Idle : FeeRelayerState
    data class ReduceAmount(val newInputAmount: BigInteger) : FeeRelayerState
    data class UpdateFee(
        val solanaFee: SendSolanaFee?,
        val feeLimitInfo: FreeTransactionFeeLimit
    ) : FeeRelayerState
    data class Failure(val errorStateError: FeeRelayerStateError) : FeeRelayerState

    fun isValidState(): Boolean = this is UpdateFee || this is ReduceAmount
}

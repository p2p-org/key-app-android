package org.p2p.wallet.send.model

import java.math.BigInteger
import org.p2p.solanaj.kits.TokenExtensionsMap
import org.p2p.wallet.feerelayer.model.TransactionFeeLimits

sealed interface FeeRelayerState {
    object Idle : FeeRelayerState
    data class ReduceAmount(
        val fee: SendSolanaFee,
        val newInputAmount: BigInteger
    ) : FeeRelayerState

    data class UpdateFee(
        val solanaFee: SendSolanaFee?,
        val feeLimitInfo: TransactionFeeLimits,
        val tokenExtensions: TokenExtensionsMap
    ) : FeeRelayerState {
        val hasToken2022Fee: Boolean
            get() = tokenExtensions.isNotEmpty()
    }

    data class Failure(
        val previousState: FeeRelayerState,
        val errorStateError: FeeRelayerStateError
    ) : FeeRelayerState, Throwable() {
        fun isFeeCalculationError(): Boolean {
            return errorStateError is FeeRelayerStateError.FeesCalculationError
        }
    }

    fun isValidState(): Boolean = this is UpdateFee || this is ReduceAmount || this is Idle
}

fun FeeRelayerState.getFee(): SendSolanaFee? {
    return when (this) {
        is FeeRelayerState.UpdateFee -> solanaFee
        is FeeRelayerState.ReduceAmount -> fee
        is FeeRelayerState.Failure -> if (previousState !is FeeRelayerState.Failure) previousState.getFee() else null
        else -> null
    }
}

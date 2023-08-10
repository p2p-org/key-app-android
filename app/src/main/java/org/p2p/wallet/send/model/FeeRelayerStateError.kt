package org.p2p.wallet.send.model

sealed interface FeeRelayerStateError {
    data class FeesCalculationError(override val cause: Throwable) : FeeRelayerStateError, Throwable()
    object InsufficientFundsToCoverFees : FeeRelayerStateError
}

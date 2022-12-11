package org.p2p.wallet.newsend.model

sealed interface FeeRelayerStateError {
    object FeesCalculationError : FeeRelayerStateError
    object InsufficientFundsToCoverFees : FeeRelayerStateError
}

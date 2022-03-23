package org.p2p.wallet.infrastructure.network.feerelayer

data class FeeRelayerErrorDetails(
    val type: FeeRelayerErrorType,
    val data: FeeRelayerErrorData
)

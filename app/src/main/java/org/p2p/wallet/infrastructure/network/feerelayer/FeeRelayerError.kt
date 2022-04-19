package org.p2p.wallet.infrastructure.network.feerelayer

class FeeRelayerError(
    val code: Int,
    val message: String?,
    val type: FeeRelayerErrorType
)

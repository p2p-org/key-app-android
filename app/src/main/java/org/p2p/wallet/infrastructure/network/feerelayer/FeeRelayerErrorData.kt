package org.p2p.wallet.infrastructure.network.feerelayer

import java.math.BigInteger

data class FeeRelayerErrorData(
    val array: List<String>?,
    val dict: Map<String, BigInteger>?
)

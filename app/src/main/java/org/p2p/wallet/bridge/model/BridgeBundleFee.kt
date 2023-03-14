package org.p2p.wallet.bridge.model

import java.math.BigDecimal

data class BridgeBundleFee(
    val amount: BigDecimal,
    val amountInUsd: BigDecimal
)

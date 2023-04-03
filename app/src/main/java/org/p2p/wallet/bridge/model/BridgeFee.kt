package org.p2p.wallet.bridge.model

import org.p2p.core.utils.orZero
import org.p2p.core.utils.toPowerValue

data class BridgeFee(
    val amount: String?,
    val amountInUsd: String?,
    val symbol: String,
    val decimals: Int,
    val chain: String?,
    val token: String?,
) {
    val amountInToken
        get() = amount?.toBigDecimal()?.orZero()?.divide(decimals.toPowerValue()).orZero()
}

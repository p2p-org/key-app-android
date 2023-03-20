package org.p2p.wallet.bridge.model

import java.math.BigDecimal
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toPowerValue

data class BridgeBundleFee(
    val amount: String?,
    val amountInUsd: String?,
    val chain: String?,
    val token: String?,
) {
    fun amountInToken(decimals: Int): BigDecimal =
        amount?.toBigDecimal()?.orZero()?.divide(decimals.toPowerValue()) ?: BigDecimal.ZERO
}

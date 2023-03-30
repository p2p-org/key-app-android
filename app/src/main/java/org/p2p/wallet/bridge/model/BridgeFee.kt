package org.p2p.wallet.bridge.model

import java.math.BigDecimal
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toPowerValue
import org.p2p.core.wrapper.eth.EthAddress

data class BridgeFee(
    val amount: String?,
    val amountInUsd: String?,
    val chain: String?,
    val token: EthAddress?,
) {
    fun amountInToken(decimals: Int): BigDecimal =
        amount?.toBigDecimal()?.orZero()?.divide(decimals.toPowerValue()) ?: BigDecimal.ZERO
}

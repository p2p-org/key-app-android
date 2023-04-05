package org.p2p.wallet.bridge.model

import org.p2p.core.utils.orZero
import org.p2p.core.utils.toPowerValue
import org.p2p.core.wrapper.eth.EthAddress

data class BridgeFee(
    val amount: String?,
    val amountInUsd: String?,
    val symbol: String,
    val decimals: Int,
    val chain: String?,
    val token: EthAddress?,
    val symbol: String,
    val name: String,
    val decimals: Int
) {
    val amountInToken
        get() = amount?.toBigDecimal()?.orZero()?.divide(decimals.toPowerValue()).orZero()
}

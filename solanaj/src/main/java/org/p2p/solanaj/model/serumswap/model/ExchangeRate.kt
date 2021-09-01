package org.p2p.solanaj.model.serumswap.model

import java.math.BigInteger

data class ExchangeRate(
    val rate: BigInteger,
    val fromDecimals: Int,
    val quoteDecimals: Int,
    val strict: Boolean
) {

    val bytes = rate.toByte() + fromDecimals + quoteDecimals + if (strict) 1 else 0
}
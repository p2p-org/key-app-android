package org.p2p.solanaj.serumswap.model

import java.math.BigInteger

data class ExchangeRate(
    val rate: BigInteger,
    val fromDecimals: Int,
    val quoteDecimals: Int,
    val strict: Boolean
) {

    val strictBytes: Int = if (strict) 1 else 0
}

package org.p2p.solanaj.serumswap.model

import java.math.BigInteger

data class ExchangeRate(
    val rate: BigInteger,
    val fromDecimals: Int,
    val quoteDecimals: Int,
    val strict: Boolean
) {

    private val strictBytes: Byte = if (strict) 1 else 0
    val bytes: Byte = (rate.toByte() + fromDecimals.toByte() + quoteDecimals.toByte() + strictBytes).toByte()
}
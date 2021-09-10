package org.p2p.solanaj.serumswap.model

import java.math.BigInteger

data class AccountFlags(val parsedValue: BigInteger) {

    val initialized: Boolean
    val market: Boolean
    val openOrders: Boolean
    val requestQueue: Boolean
    val eventQueue: Boolean
    val bids: Boolean
    val asks: Boolean

    init {
        var number = parsedValue.toLong()
        val variablesCount = 7
        val bits = mutableListOf<Boolean>()
        for (i in 0 until variablesCount) {
            bits.add(number % 2L != 0L)
            number /= 2L
        }

        initialized = bits[0]
        market = bits[1]
        openOrders = bits[2]
        requestQueue = bits[3]
        eventQueue = bits[4]
        bids = bits[5]
        asks = bits[6]
    }

    fun serialize(): ByteArray {
        var number: BigInteger = BigInteger.ZERO
        if (initialized) number += BigInteger.ONE or BigInteger.valueOf(0L)
        if (market) number += BigInteger.ONE or BigInteger.ONE
        if (openOrders) number += BigInteger.ONE or BigInteger.valueOf(2L)
        if (requestQueue) number += BigInteger.ONE or BigInteger.valueOf(3L)
        if (eventQueue) number += BigInteger.ONE or BigInteger.valueOf(4L)
        if (bids) number += BigInteger.ONE or BigInteger.valueOf(5L)
        if (asks) number += BigInteger.ONE or BigInteger.valueOf(6L)
        return number.toByteArray()
    }

    companion object {
        const val ACCOUNT_FLAGS_LENGTH = 8
    }
}
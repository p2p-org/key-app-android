package org.p2p.solanaj.serumswap.model

import org.p2p.solanaj.model.core.AbstractData
import org.p2p.solanaj.utils.ByteUtils

class AccountFlags constructor(data: ByteArray) : AbstractData(data, ACCOUNT_FLAGS_LENGTH) {

    val initialized: Boolean
    val market: Boolean
    val openOrders: Boolean
    val requestQueue: Boolean
    val eventQueue: Boolean
    val bids: Boolean
    val asks: Boolean

    init {
        var number = ByteUtils.readUint64(data, 0).toLong()

        val variablesCount = 7
        val bits = mutableListOf<Boolean>()
        for (i in 0..variablesCount) {
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

    companion object {
        const val ACCOUNT_FLAGS_LENGTH = 8
    }
}
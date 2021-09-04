package org.p2p.solanaj.serumswap

import org.p2p.solanaj.model.core.AbstractData
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.serumswap.model.AccountFlags
import java.math.BigInteger

private const val LAYOUT_V1_SPAN = 160

sealed class OpenOrdersLayout(
    val data: ByteArray,
    val length: Int
) : AbstractData(data, length) {

    val accountFlags: AccountFlags = AccountFlags(data)

    val market: PublicKey = readPublicKey()
    val owner: PublicKey = readPublicKey()
    val baseTokenFree: BigInteger = readUint64()
    val baseTokenTotal: BigInteger = readUint64()
    val quoteTokenFree: BigInteger = readUint64()
    val quoteTokenTotal: BigInteger = readUint64()

    val freeSlotBits: PublicKey = readPublicKey()
    val isBidBits: PublicKey = readPublicKey()

    val orders: List<PublicKey> = emptyList() // fixme: add parsing
    val clientIds: List<BigInteger> = emptyList() // fixme: add parsing

    class LayoutV1 constructor(data: ByteArray) : OpenOrdersLayout(data, LAYOUT_V1_SPAN) {
        fun getSpanSum(): Int = 141 + orders.size + clientIds.size + 7
    }

    class LayoutV2 constructor(data: ByteArray) : OpenOrdersLayout(data, LAYOUT_V1_SPAN) {
        val referrerRebatesAccrued: BigInteger = readUint64()

        fun getSpanSum(layoutV1Span: Long): Long = layoutV1Span + 8
    }
}
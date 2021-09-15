package org.p2p.solanaj.serumswap

import org.p2p.solanaj.core.AbstractData
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.serumswap.model.AccountFlags
import org.p2p.solanaj.serumswap.model.Integer128
import org.p2p.solanaj.serumswap.model.MemoryLayout
import org.p2p.solanaj.serumswap.model.Seq128Elements
import java.math.BigInteger

private const val LAYOUT_V1_SPAN = 160L

sealed class OpenOrdersLayout(
    val data: ByteArray,
    val length: Int
) : AbstractData(data, length) {

    lateinit var accountFlags: AccountFlags
    lateinit var market: PublicKey
    lateinit var owner: PublicKey
    lateinit var baseTokenFree: BigInteger
    lateinit var baseTokenTotal: BigInteger
    lateinit var quoteTokenFree: BigInteger
    lateinit var quoteTokenTotal: BigInteger
    lateinit var freeSlotBits: Integer128
    lateinit var isBidBits: Integer128
    lateinit var orders: Seq128Elements<Integer128>
    lateinit var clientIds: Seq128Elements<BigInteger>

    open fun initialize() {
        accountFlags = AccountFlags(readUint64())

        market = readPublicKey()
        owner = readPublicKey()
        baseTokenFree = readUint64()
        baseTokenTotal = readUint64()
        quoteTokenFree = readUint64()
        quoteTokenTotal = readUint64()

        freeSlotBits = Integer128(readUint128())
        isBidBits = Integer128(readUint128())

        orders = readSeq128Elements(MemoryLayout.BigInteger128) as Seq128Elements<Integer128>
        clientIds = readSeq128Elements(MemoryLayout.BigInteger) as Seq128Elements<BigInteger>
    }

    enum class Type(val span: Long) {
        LAYOUT_V1(LAYOUT_V1_SPAN),
        LAYOUT_V2(LAYOUT_V1_SPAN + 8);
    }

    class LayoutV1 constructor(data: ByteArray) : OpenOrdersLayout(data, LAYOUT_V1_SPAN.toInt()) {

        init {
            initialize()
        }

        fun getSpanSum(): Int = 141 + orders.length + clientIds.length + 7
    }

    class LayoutV2 constructor(data: ByteArray) : OpenOrdersLayout(data, LAYOUT_V1_SPAN.toInt()) {
        val referrerRebatesAccrued: BigInteger

        init {
            initialize()
            referrerRebatesAccrued = readUint64()
        }

        fun getSpanSum(layoutV1Span: Long): Long = layoutV1Span + 8
    }
}

fun ByteArray.chunked(into: Int): List<ByteArray> {
    val bytes = mutableListOf<Int>()
    for (i in 0..this.size step into) {
        bytes.add(i)
    }

    return bytes.map { this.copyOfRange(it, minOf(it + into, size)) }
}
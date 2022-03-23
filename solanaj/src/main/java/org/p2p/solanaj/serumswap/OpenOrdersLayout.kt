package org.p2p.solanaj.serumswap

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.serumswap.OpenOrdersLayoutParser.LAYOUT_V1_SPAN
import org.p2p.solanaj.serumswap.model.AccountFlags
import org.p2p.solanaj.serumswap.model.Integer128
import java.math.BigInteger

sealed class OpenOrdersLayout(
    open val accountFlags: AccountFlags,
    open val market: PublicKey,
    open val owner: PublicKey,
    open val baseTokenFree: BigInteger,
    open val baseTokenTotal: BigInteger,
    open val quoteTokenFree: BigInteger,
    open val quoteTokenTotal: BigInteger,
    open val freeSlotBits: Integer128,
    open val isBidBits: Integer128,
    open val orders: Any,
    open val clientIds: Any
) {

    data class LayoutV1(
        override val accountFlags: AccountFlags,
        override val market: PublicKey,
        override val owner: PublicKey,
        override val baseTokenFree: BigInteger,
        override val baseTokenTotal: BigInteger,
        override val quoteTokenFree: BigInteger,
        override val quoteTokenTotal: BigInteger,
        override val freeSlotBits: Integer128,
        override val isBidBits: Integer128,
        override val orders: Any,
        override val clientIds: Any
    ) : OpenOrdersLayout(
        accountFlags = accountFlags,
        market = market,
        owner = owner,
        baseTokenFree = baseTokenFree,
        baseTokenTotal = baseTokenTotal,
        quoteTokenFree = quoteTokenFree,
        quoteTokenTotal = quoteTokenTotal,
        freeSlotBits = freeSlotBits,
        isBidBits = isBidBits,
        orders = orders,
        clientIds = clientIds
    )

    data class LayoutV2(
        override val accountFlags: AccountFlags,
        override val market: PublicKey,
        override val owner: PublicKey,
        override val baseTokenFree: BigInteger,
        override val baseTokenTotal: BigInteger,
        override val quoteTokenFree: BigInteger,
        override val quoteTokenTotal: BigInteger,
        override val freeSlotBits: Integer128,
        override val isBidBits: Integer128,
        override val orders: Any,
        override val clientIds: Any,
        val referrerRebatesAccrued: BigInteger
    ) : OpenOrdersLayout(
        accountFlags = accountFlags,
        market = market,
        owner = owner,
        baseTokenFree = baseTokenFree,
        baseTokenTotal = baseTokenTotal,
        quoteTokenFree = quoteTokenFree,
        quoteTokenTotal = quoteTokenTotal,
        freeSlotBits = freeSlotBits,
        isBidBits = isBidBits,
        orders = orders,
        clientIds = clientIds
    )

    enum class Type(val span: Long) {
        LAYOUT_V1(LAYOUT_V1_SPAN),
        LAYOUT_V2(LAYOUT_V1_SPAN + 8);
    }
}

fun ByteArray.chunked(into: Int): List<ByteArray> {
    val bytes = mutableListOf<Int>()
    for (i in 0..this.size step into) {
        bytes.add(i)
    }

    return bytes.map { this.copyOfRange(it, minOf(it + into, size)) }
}

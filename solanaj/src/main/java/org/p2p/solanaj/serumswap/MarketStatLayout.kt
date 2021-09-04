package org.p2p.solanaj.serumswap

import org.p2p.solanaj.model.core.AbstractData
import org.p2p.solanaj.model.core.PublicKey
import org.p2p.solanaj.serumswap.model.AccountFlags
import java.math.BigInteger

private const val LAYOUT_V1_SPAN = 380L

open class MarketStatLayout(
    val data: ByteArray,
    val length: Int
) : AbstractData(data, length) {

    val accountFlags: AccountFlags = AccountFlags(data)
    val ownAddress: PublicKey = readPublicKey()
    val vaultSignerNonce: BigInteger = readUint64()
    val baseMint: PublicKey = readPublicKey()
    val quoteMint: PublicKey = readPublicKey()
    val baseVault: PublicKey = readPublicKey()
    val baseDepositsTotal: BigInteger = readUint64()
    val baseFeesAccrued: BigInteger = readUint64()
    val quoteVault: PublicKey = readPublicKey()
    val quoteDepositsTotal: BigInteger = readUint64()
    val quoteFeesAccrued: BigInteger = readUint64()
    val quoteDustThreshold: BigInteger = readUint64()
    val requestQueue: PublicKey = readPublicKey()
    val eventQueue: PublicKey = readPublicKey()
    val bids: PublicKey = readPublicKey()
    val asks: PublicKey = readPublicKey()
    val baseLotSize: BigInteger = readUint64()
    val quoteLotSize: BigInteger = readUint64()
    val feeRateBps: BigInteger = readUint64()

    enum class Type(val span: Long) {
        LAYOUT_V1(LAYOUT_V1_SPAN),
        LAYOUT_V2(LAYOUT_V1_SPAN + 8);
    }

    class LayoutV1 constructor(data: ByteArray) : MarketStatLayout(data, LAYOUT_V1_SPAN.toInt())

    class LayoutV2 constructor(data: ByteArray) : MarketStatLayout(data, LAYOUT_V1_SPAN.toInt()) {
        val referrerRebatesAccrued: BigInteger = readUint64()
    }
}
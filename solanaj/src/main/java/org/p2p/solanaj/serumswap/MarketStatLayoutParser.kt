package org.p2p.solanaj.serumswap

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.serumswap.model.AccountFlags
import org.p2p.solanaj.utils.ByteUtils
import java.math.BigInteger

object MarketStatLayoutParser {

    private const val LAYOUT_V1_SPAN = 380L

    @Transient
    private var data: ByteArray = byteArrayOf()

    @Transient
    private var cursor = 0

    fun parseV1(data: ByteArray): MarketStatLayout {
        this.data = data
        cursor = 0

        require(data.size >= LAYOUT_V1_SPAN) { "Wrong data" }

        // skipping five bytes
        cursor += 5
        return MarketStatLayout.LayoutV1(
            accountFlags = AccountFlags(readUint64()),
            ownAddress = readPublicKey(),
            vaultSignerNonce = readUint64(),
            baseMint = readPublicKey(),
            quoteMint = readPublicKey(),
            baseVault = readPublicKey(),
            baseDepositsTotal = readUint64(),
            baseFeesAccrued = readUint64(),
            quoteVault = readPublicKey(),
            quoteDepositsTotal = readUint64(),
            quoteFeesAccrued = readUint64(),
            quoteDustThreshold = readUint64(),
            requestQueue = readPublicKey(),
            eventQueue = readPublicKey(),
            bids = readPublicKey(),
            asks = readPublicKey(),
            baseLotSize = readUint64(),
            quoteLotSize = readUint64(),
            feeRateBps = readUint64()
        )
    }

    fun parseV2(data: ByteArray): MarketStatLayout {
        this.data = data
        cursor = 0

        require(data.size >= LAYOUT_V1_SPAN + 8) { "Wrong data" }

        // skipping five bytes
        cursor += 5
        return MarketStatLayout.LayoutV2(
            accountFlags = AccountFlags(readUint64()),
            ownAddress = readPublicKey(),
            vaultSignerNonce = readUint64(),
            baseMint = readPublicKey(),
            quoteMint = readPublicKey(),
            baseVault = readPublicKey(),
            baseDepositsTotal = readUint64(),
            baseFeesAccrued = readUint64(),
            quoteVault = readPublicKey(),
            quoteDepositsTotal = readUint64(),
            quoteFeesAccrued = readUint64(),
            quoteDustThreshold = readUint64(),
            requestQueue = readPublicKey(),
            eventQueue = readPublicKey(),
            bids = readPublicKey(),
            asks = readPublicKey(),
            baseLotSize = readUint64(),
            quoteLotSize = readUint64(),
            feeRateBps = readUint64(),
            referrerRebatesAccrued = readUint64()
        )
    }

    private fun readPublicKey(): PublicKey {
        val pk = PublicKey.readPubkey(data, cursor)
        cursor += PublicKey.PUBLIC_KEY_LENGTH
        return pk
    }

    private fun readUint64(): BigInteger {
        val uint64 = ByteUtils.readUint64(data, cursor)
        cursor += ByteUtils.UINT_64_LENGTH
        return uint64
    }
}

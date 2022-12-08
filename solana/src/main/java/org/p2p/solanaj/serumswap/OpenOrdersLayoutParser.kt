package org.p2p.solanaj.serumswap

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.serumswap.model.AccountFlags
import org.p2p.solanaj.serumswap.model.Integer128
import org.p2p.solanaj.serumswap.model.MemoryLayout
import org.p2p.solanaj.serumswap.model.Seq128Elements
import org.p2p.solanaj.utils.ByteUtils
import java.math.BigInteger

object OpenOrdersLayoutParser {

    const val LAYOUT_V1_SPAN =
        141L +
            (128L * 16L) +
            (128L * 8L) +
            7L

    @Transient
    private var data: ByteArray = byteArrayOf()

    @Transient
    private var cursor = 0

    fun parseV1(data: ByteArray): OpenOrdersLayout {
        this.data = data
        cursor = 0

        require(data.size >= LAYOUT_V1_SPAN) { "Wrong data" }

        // skipping five bytes
        cursor += 5
        return OpenOrdersLayout.LayoutV1(
            accountFlags = AccountFlags(readUint64()),
            market = readPublicKey(),
            owner = readPublicKey(),
            baseTokenFree = readUint64(),
            baseTokenTotal = readUint64(),
            quoteTokenFree = readUint64(),
            quoteTokenTotal = readUint64(),
            freeSlotBits = Integer128(readUint128()),
            isBidBits = Integer128(readUint128()),
            orders = readBytes(Seq128Elements.LENGTH * MemoryLayout.BigInteger128.getSize()),
            clientIds = readBytes(Seq128Elements.LENGTH * MemoryLayout.BigInteger.getSize()),
        )
    }

    fun parseV2(data: ByteArray): OpenOrdersLayout {
        this.data = data
        cursor = 0

        require(data.size >= LAYOUT_V1_SPAN) { "Wrong data" }

        // skipping five bytes
        cursor += 5
        return OpenOrdersLayout.LayoutV2(
            accountFlags = AccountFlags(readUint64()),
            market = readPublicKey(),
            owner = readPublicKey(),
            baseTokenFree = readUint64(),
            baseTokenTotal = readUint64(),
            quoteTokenFree = readUint64(),
            quoteTokenTotal = readUint64(),
            freeSlotBits = Integer128(readUint128()),
            isBidBits = Integer128(readUint128()),
            orders = readBytes(Seq128Elements.LENGTH * MemoryLayout.BigInteger128.getSize()),
            clientIds = readBytes(Seq128Elements.LENGTH * MemoryLayout.BigInteger.getSize()),
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

    private fun readUint128(): BigInteger {
        val uint128 = ByteUtils.readUint128(data, cursor)
        cursor += ByteUtils.UINT_128_LENGTH
        return uint128
    }

    private fun readBytes(length: Int): ByteArray {
        val bytes = ByteUtils.readBytes(data, cursor, length)
        cursor += length
        return bytes
    }
}

package org.p2p.solanaj.core

import org.bitcoinj.core.Utils
import org.p2p.solanaj.serumswap.chunked
import org.p2p.solanaj.serumswap.model.MemoryLayout
import org.p2p.solanaj.serumswap.model.Seq128Elements
import org.p2p.solanaj.utils.ByteUtils
import java.math.BigInteger

abstract class AbstractData protected constructor(data: ByteArray, dataLength: Int) {
    @Transient
    private val data: ByteArray

    @Transient
    private var cursor = 0

    init {
        require(data.size >= dataLength) { "Wrong data" }
        this.data = data
    }

    protected fun getCursorPosition(): Int = cursor

    protected fun setCursorPosition(position: Int) {
        cursor = position
    }

    protected fun readBytes(length: Int): ByteArray {
        val bytes = ByteUtils.readBytes(data, cursor, length)
        cursor += length
        return bytes
    }

    protected fun readByte(): Byte {
        return data[cursor++]
    }

    protected fun readPublicKey(): PublicKey {
        val pk = PublicKey.readPubkey(data, cursor)
        cursor += PublicKey.PUBLIC_KEY_LENGTH
        return pk
    }

    protected fun readUint32(): Long {
        val value = Utils.readUint32(data, cursor)
        cursor += ByteUtils.UINT_32_LENGTH
        return value
    }

    protected fun readUint64(): BigInteger {
        val uint64 = ByteUtils.readUint64(data, cursor)
        cursor += ByteUtils.UINT_64_LENGTH
        return uint64
    }

    protected fun readUint128(): BigInteger {
        val uint128 = ByteUtils.readUint128(data, cursor)
        cursor += ByteUtils.UINT_128_LENGTH
        return uint128
    }

    protected fun readSeq128Elements(memoryLayout: MemoryLayout): Seq128Elements<Any> {
        val length = Seq128Elements.LENGTH + memoryLayout.getSize()
        val endIndex = cursor + length
        if (data.size > endIndex) throw IllegalStateException("Bytes length is not valid")

        val bytesArray = data.copyOfRange(cursor, endIndex - 1)
        val elements = mutableListOf<Any>()

        val chunkedArray = bytesArray.chunked(memoryLayout.getSize())

        chunkedArray.forEachIndexed { index, bytes ->
            when (memoryLayout) {
                is MemoryLayout.Byte -> {
                    val element = bytes[index]
                    elements.add(element)
                }
                is MemoryLayout.Long -> {
                    val element = Utils.readUint32(bytes, index)
                    elements.add(element)
                }
                is MemoryLayout.BigInteger -> {
                    val element = ByteUtils.readUint64(bytes, index)
                    elements.add(element)
                }
                is MemoryLayout.BigInteger128 -> {
                    val element = ByteUtils.readUint128(bytes, index)
                    elements.add(element)
                }
                is MemoryLayout.PublicKey -> {
                    val element = PublicKey.readPubkey(bytes, index)
                    elements.add(element)
                }
            }
        }

        cursor += length
        return Seq128Elements(elements, length)
    }
}

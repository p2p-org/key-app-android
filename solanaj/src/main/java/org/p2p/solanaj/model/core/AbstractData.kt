package org.p2p.solanaj.model.core

import org.bitcoinj.core.Utils
import org.p2p.solanaj.utils.ByteUtils
import java.math.BigInteger

abstract class AbstractData protected constructor(data: ByteArray, dataLength: Int) {
    @Transient
    private val data: ByteArray

    @Transient
    private var cursor = 0

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

    init {
        require(data.size >= dataLength) { "Wrong data" }
        this.data = data
    }
}
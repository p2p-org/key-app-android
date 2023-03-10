package org.p2p.ethereumkit.internal.models

import org.p2p.ethereumkit.internal.core.AddressValidator
import org.p2p.ethereumkit.internal.core.hexStringToByteArray
import org.p2p.ethereumkit.internal.core.toHexString
import org.p2p.ethereumkit.internal.utils.EIP55

data class EthAddress(var raw: ByteArray) {
    init {
        if (raw.size == 32) {
            raw = raw.copyOfRange(12, raw.size)
        }
        AddressValidator.validate(hex)
    }

    constructor(hex: String) : this(hex.hexStringToByteArray())

    val hex: String
        get() = raw.toHexString()

    val eip55: String
        get() = EIP55.format(hex)

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        return if (other is EthAddress)
            raw.contentEquals(other.raw)
        else false
    }

    override fun hashCode(): Int {
        return raw.contentHashCode()
    }

    override fun toString(): String {
        return hex
    }

}

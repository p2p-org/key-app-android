package org.p2p.core.wrapper.eth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EthAddress(var raw: ByteArray) : Parcelable {
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

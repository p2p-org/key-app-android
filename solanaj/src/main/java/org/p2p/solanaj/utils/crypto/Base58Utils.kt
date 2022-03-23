package org.p2p.solanaj.utils.crypto

import org.bitcoinj.core.Base58

object Base58Utils {
    fun encode(bytes: ByteArray): String =
        Base58.encode(bytes)

    fun decode(stringToDecode: String): ByteArray =
        Base58.decode(stringToDecode)

    fun encodeFromString(string: String) = encode(string.toByteArray())

    fun decodeToString(data: String) = String(decode(data))
}

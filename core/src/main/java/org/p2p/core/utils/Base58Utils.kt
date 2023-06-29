package org.p2p.core.utils

import org.bitcoinj.core.Base58

object Base58Utils {
    fun encode(bytes: ByteArray): String =
        Base58.encode(bytes)

    fun decode(stringToDecode: String): ByteArray =
        Base58.decode(stringToDecode)

    fun encodeFromString(string: String) = encode(string.toByteArray())

    fun decodeToString(data: String) = String(decode(data))

    fun isValidBase58(data: String): Boolean = kotlin.runCatching { data.decodeFromBase58() }.isSuccess
}

fun ByteArray.encodeToBase58(): String = Base58Utils.encode(this)
fun String.decodeFromBase58(): ByteArray = Base58Utils.decode(this)

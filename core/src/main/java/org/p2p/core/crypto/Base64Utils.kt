package org.p2p.core.crypto

import android.util.Base64

object Base64Utils {

    fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)
            ?: java.util.Base64.getEncoder().encodeToString(bytes)

    fun decode(stringToDecode: String): ByteArray =
        Base64.decode(stringToDecode, Base64.DEFAULT)
            ?: java.util.Base64.getDecoder().decode(stringToDecode)

    fun encodeFromString(string: String) = encode(string.toByteArray())

    fun decodeToString(data: String) = String(decode(data))

    fun isValidBase64(data: String): Boolean = kotlin.runCatching { data.decodeFromBase64() }.isSuccess
}

fun ByteArray.encodeToBase64(): String = Base64Utils.encode(bytes = this)
fun String.decodeFromBase64(): ByteArray = Base64Utils.decode(stringToDecode = this)

fun ByteArray.toBase64Instance(): Base64String = Base64String(bytes = this)
fun String.toBase64Instance(): Base64String = Base64String(base64Value = this)

data class Base64String(val base64Value: String) {
    constructor(bytes: ByteArray) : this(bytes.copyOf().encodeToBase64())

    fun decodeToBytes(): ByteArray = base64Value.decodeFromBase64()

    override fun toString(): String = base64Value
}

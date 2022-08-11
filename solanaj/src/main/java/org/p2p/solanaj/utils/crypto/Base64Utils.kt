package org.p2p.solanaj.utils.crypto

import android.util.Base64

object Base64Utils {

    fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    fun decode(stringToDecode: String): ByteArray =
        Base64.decode(stringToDecode, Base64.DEFAULT)

    fun encodeFromString(string: String) = encode(string.toByteArray())

    fun decodeToString(data: String) = String(decode(data))

    fun isValidBase64(data: String): Boolean = kotlin.runCatching { data.decodeFromBase64() }.isSuccess
}

fun ByteArray.encodeToBase64(): String = Base64Utils.encode(bytes = this)
fun String.decodeFromBase64(): ByteArray = Base64Utils.decode(stringToDecode = this)

fun ByteArray.encodeToBase64String(): Base64String = Base64String(bytes = this)
fun String.toBase64String(): Base64String = Base64String(base64Value = this)

class Base64String(val base64Value: String) {
    constructor(bytes: ByteArray) : this(bytes.copyOf().encodeToBase64())
    fun decode(): ByteArray = base64Value.decodeFromBase58()
}

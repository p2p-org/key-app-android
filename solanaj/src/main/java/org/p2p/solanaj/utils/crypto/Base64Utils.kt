package org.p2p.solanaj.utils.crypto

import android.util.Base64

object Base64Utils {

    fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    fun decode(stringToDecode: String): ByteArray =
        Base64.decode(stringToDecode, Base64.DEFAULT)

    fun encodeFromString(string: String) = encode(string.toByteArray())

    fun decodeToString(data: String) = String(decode(data))
}

package org.p2p.core.crypto

private const val HEX_ARRAY = "0123456789abcdef"

@Suppress("MagicNumber")
object Hex {

    fun encode(bytes: ByteArray): String {

        val hexChars = CharArray(bytes.size * 2)

        for (i in bytes.indices) {
            val firstCharIndex = i * 2
            val byte = bytes[i].toInt() and 0xFF
            hexChars[firstCharIndex] = HEX_ARRAY[byte ushr 4]
            hexChars[firstCharIndex + 1] = HEX_ARRAY[byte and 0x0F]
        }
        return String(hexChars)
    }

    fun decode(hexString: String): ByteArray {
        val bytes = ByteArray(hexString.length / 2)

        for (i in bytes.indices) {
            val firstCharIndex = i * 2
            val highBytes = Character.digit(hexString[firstCharIndex], 16) shl 4
            val lowBytes = Character.digit(hexString[firstCharIndex + 1], 16)

            bytes[i] = (highBytes + lowBytes).toByte()
        }

        return bytes
    }
}

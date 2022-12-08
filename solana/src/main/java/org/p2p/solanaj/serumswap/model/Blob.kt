package org.p2p.solanaj.serumswap.model

data class Blob(
    val length: Int,
    val bytes: ByteArray = ByteArray(0)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Blob

        if (length != other.length) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = length
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

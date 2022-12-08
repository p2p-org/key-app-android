package org.p2p.solanaj.serumswap.model

sealed class MemoryLayout {

    object Byte : MemoryLayout()
    object Long : MemoryLayout()
    object BigInteger : MemoryLayout()
    object BigInteger128 : MemoryLayout()
    object PublicKey : MemoryLayout()

    fun getSize(): Int = when (this) {
        is Byte -> 1
        is Long -> 4
        is BigInteger -> 8
        is BigInteger128 -> 16
        is PublicKey -> 32
    }
}

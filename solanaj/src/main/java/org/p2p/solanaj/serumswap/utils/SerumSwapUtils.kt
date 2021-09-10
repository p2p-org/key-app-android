package org.p2p.solanaj.serumswap.utils

import org.bitcoinj.core.Sha256Hash

const val SIGHASH_GLOBAL_NAMESPACE = "global"

object SerumSwapUtils {

    fun sighash(
        namespace: String = SIGHASH_GLOBAL_NAMESPACE,
        ixName: String
    ): ByteArray {
        val name = ixName.snakeCased()
        val preimage = "$namespace:$name"
        return Sha256Hash.hash(preimage.toByteArray()).copyOfRange(0, 8)
    }
}

fun String.snakeCased(): String {
    val regex = "(?<=[a-zA-Z])[A-Z]".toRegex()
    return regex.replace(this) { "_${it.value}" }.lowercase()
}
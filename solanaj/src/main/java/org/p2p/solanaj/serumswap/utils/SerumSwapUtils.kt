package org.p2p.solanaj.serumswap.utils

import org.p2p.solanaj.crypto.Hash

const val SIGHASH_GLOBAL_NAMESPACE = "global"

object SerumSwapUtils {

    fun sighash(
        namespace: String = SIGHASH_GLOBAL_NAMESPACE,
        ixName: String
    ): ByteArray {
        val name = ixName.snakeCased()
        val preimage = "$namespace:$name"
        return Hash.sha256(preimage.toByteArray()).copyOfRange(0, 8)
    }
}

fun String.snakeCased(): String {
    val regex = "(?<=[a-zA-Z])[A-Z]".toRegex()
    return regex.replace(this) { "_${it.value}" }.lowercase()
}
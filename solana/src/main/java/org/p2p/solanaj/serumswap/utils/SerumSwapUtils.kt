package org.p2p.solanaj.serumswap.utils

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.crypto.Hash
import org.p2p.solanaj.programs.SerumSwapProgram
import org.p2p.solanaj.utils.ByteUtils
import java.io.ByteArrayOutputStream

const val SIGHASH_GLOBAL_NAMESPACE = "global"

object SerumSwapUtils {

    @Throws(Exception::class)
    fun getVaultOwnerAndNonce(
        marketPublicKey: PublicKey,
        dexProgramId: PublicKey = SerumSwapProgram.dexPID
    ): PublicKey {

        var nonce = 0

        while (nonce < 255) {
            try {
                val bos = ByteArrayOutputStream()
                ByteUtils.uint64ToByteStreamLE(nonce.toLong().toBigInteger(), bos)
                return PublicKey.createProgramAddress(
                    seeds = listOf(marketPublicKey.asByteArray(), bos.toByteArray()),
                    programId = dexProgramId
                )
            } catch (e: Exception) {
                nonce += 1
            }
        }

        throw IllegalStateException("Could not find vault owner")
    }

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

package org.p2p.solanaj.core

import org.bitcoinj.core.Base58
import org.bitcoinj.core.Sha256Hash
import org.p2p.solanaj.serumswap.instructions.SerumSwapInstructions
import org.p2p.solanaj.utils.ByteUtils
import org.p2p.solanaj.utils.TweetNaclFast
import java.io.ByteArrayOutputStream
import java.math.BigInteger

class PublicKey {
    private var pubkey: ByteArray

    constructor(pubkey: String) {
        require(pubkey.length >= PUBLIC_KEY_LENGTH) { "Invalid public key input" }
        this.pubkey = Base58.decode(pubkey)
    }

    constructor(pubkey: ByteArray) {
        require(pubkey.size <= PUBLIC_KEY_LENGTH) { "Invalid public key input" }
        this.pubkey = pubkey
    }

    fun toByteArray(): ByteArray {
        return pubkey
    }

    fun toBase58(): String {
        return Base58.encode(pubkey)
    }

    fun equals(pubkey: PublicKey): Boolean {
        return this.pubkey.contentEquals(pubkey.toByteArray())
    }

    override fun toString(): String {
        return toBase58()
    }

    class ProgramDerivedAddress(val address: PublicKey, val nonce: Int)

    companion object {
        const val PUBLIC_KEY_LENGTH = 32
        fun readPubkey(bytes: ByteArray, offset: Int): PublicKey {
            val buf = ByteUtils.readBytes(bytes, offset, PUBLIC_KEY_LENGTH)
            return PublicKey(buf)
        }

        @Throws(Exception::class)
        fun createProgramAddress(seeds: List<ByteArray>, programId: PublicKey): PublicKey {
            val buffer = ByteArrayOutputStream()
            for (seed in seeds) {
                require(seed.size <= 32) { "Max seed length exceeded" }
                buffer.write(seed)
            }
            buffer.write(programId.toByteArray())
            buffer.write("ProgramDerivedAddress".toByteArray())
            val hash = Sha256Hash.hash(buffer.toByteArray())
            if (TweetNaclFast.is_on_curve(hash) != 0) {
                throw Exception("Invalid seeds, address must fall off the curve")
            }
            return PublicKey(hash)
        }

        @Throws(Exception::class)
        fun findProgramAddress(seeds: List<ByteArray>, programId: PublicKey): ProgramDerivedAddress {
            var nonce = 255
            val address: PublicKey
            val seedsWithNonce: MutableList<ByteArray> = ArrayList()
            seedsWithNonce.addAll(seeds)
            while (nonce != 0) {
                address = try {
                    seedsWithNonce.add(byteArrayOf(nonce.toByte()))
                    createProgramAddress(seedsWithNonce, programId)
                } catch (e: Exception) {
                    seedsWithNonce.removeAt(seedsWithNonce.size - 1)
                    nonce--
                    continue
                }
                return ProgramDerivedAddress(address, nonce)
            }
            throw Exception("Unable to find a viable program address nonce")
        }

        @Throws(Exception::class)
        fun getVaultOwnerAndNonce(
            marketPublicKey: PublicKey,
            dexProgramId: PublicKey = SerumSwapInstructions.dexPID
        ): PublicKey {

            var nonce = BigInteger.ZERO

            while (nonce < BigInteger.valueOf(255L)) {
                try {
                    return createProgramAddress(
                        seeds = listOf(
                            marketPublicKey.toByteArray(),
                            nonce.toByteArray()
                        ),
                        programId = dexProgramId
                    )
                } catch (e: Exception) {
                    nonce += BigInteger.ONE
                }
            }

            throw IllegalStateException("Could not find vault owner")
        }
    }
}
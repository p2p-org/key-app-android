package org.p2p.solanaj.core

import org.bitcoinj.core.Base58
import org.bitcoinj.core.Sha256Hash
import org.p2p.solanaj.utils.ByteUtils
import org.p2p.solanaj.utils.PublicKeyValidator
import org.p2p.solanaj.utils.TweetNaclFast
import java.io.ByteArrayOutputStream
import org.p2p.core.crypto.Base58String

class PublicKey {
    private var publicKey: ByteArray

    @Throws(IllegalArgumentException::class)
    constructor(publicKey: String) {
        require(PublicKeyValidator.isValid(publicKey)) {
            "Invalid public key input[String]"
        }

        this.publicKey = Base58.decode(publicKey)
    }

    @Throws(IllegalArgumentException::class)
    constructor(publicKey: ByteArray) {
        require(PublicKeyValidator.isValid(publicKey)) {
            "Invalid public key input[ByteArray]"
        }

        this.publicKey = publicKey
    }

    fun asByteArray(): ByteArray = publicKey.copyOf()

    fun toBase58(): String = Base58.encode(publicKey)

    fun equals(other: PublicKey): Boolean = this.publicKey.contentEquals(other.asByteArray())

    override fun toString(): String = toBase58()

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
            buffer.write(programId.asByteArray())
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
    }
}
fun PublicKey.toBase58Instance(): Base58String = Base58String(this.toBase58())

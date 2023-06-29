package org.p2p.core.crypto

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val SALT_GENERATE_ALGORITHM = "SHA1PRNG"
private const val SALT_SIZE = 16

private const val PIN_HASH_ALGORITHM = "PBKDF2WithHmacSHA1"

private const val PBKDF2_ITERATIONS = 5000
private const val PBKDF2_DEFAULT_LENGTH_BYTES = 64

class Pbkdf2Hash(
    val hashResultInHex: String,
    val hashSalt: ByteArray
)

class Pbkdf2HashGenerator {

    fun generateHashWithRandomSalt(data: String): Pbkdf2Hash {
        return generateHash(data, generateRandomSalt())
    }

    fun generateHash(data: String, salt: ByteArray): Pbkdf2Hash {
        require(data.isNotBlank())

        val dataToHash: CharArray = data.toCharArray()
        val keyLengthBytes: Int = PBKDF2_DEFAULT_LENGTH_BYTES * Byte.SIZE_BITS
        val hashSalt: ByteArray = salt

        val hashingSpec = PBEKeySpec(
            dataToHash,
            hashSalt,
            PBKDF2_ITERATIONS,
            keyLengthBytes
        )
        val keyFactory = SecretKeyFactory.getInstance(PIN_HASH_ALGORITHM)
        val hashResult = keyFactory.generateSecret(hashingSpec).encoded

        return Pbkdf2Hash(
            hashResultInHex = Hex.encode(hashResult),
            hashSalt = hashSalt
        )
    }

    private fun generateRandomSalt(): ByteArray {
        val bytes = ByteArray(SALT_SIZE)
        val random = SecureRandom.getInstance(SALT_GENERATE_ALGORITHM)
        random.nextBytes(bytes)
        return bytes
    }
}

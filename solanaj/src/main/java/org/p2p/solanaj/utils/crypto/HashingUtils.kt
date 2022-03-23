package org.p2p.solanaj.utils.crypto

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val SALT_GENERATE_ALGORITHM = "SHA1PRNG"
private const val SALT_SIZE = 16

private const val PIN_HASH_ALGORITHM = "PBKDF2WithHmacSHA1"

private const val PBKDF2_ITERATIONS = 5000
private const val PBKDF2_DEFAULT_LENGTH_BYTES = 64

object HashingUtils {

    fun generatePbkdf2Hex(data: String, salt: ByteArray, lengthBytes: Int = PBKDF2_DEFAULT_LENGTH_BYTES): String =
        Hex.encode(generatePbkdf2(data, salt, lengthBytes))

    fun generatePbkdf2(data: String, salt: ByteArray, keyLengthBytes: Int = PBKDF2_DEFAULT_LENGTH_BYTES): ByteArray {
        val chars = data.toCharArray()

        val spec = PBEKeySpec(chars, salt, PBKDF2_ITERATIONS, keyLengthBytes * Byte.SIZE_BITS)
        val keyFactory = SecretKeyFactory.getInstance(PIN_HASH_ALGORITHM)

        return keyFactory.generateSecret(spec).encoded
    }

    fun generateSalt(): ByteArray {
        val random = SecureRandom.getInstance(SALT_GENERATE_ALGORITHM)
        val bytes = ByteArray(SALT_SIZE)
        random.nextBytes(bytes)
        return bytes
    }
}

package com.p2p.wowlet.utils.cipher

import android.util.Base64
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class Cipher {
    companion object {
        fun decrypt(cipherText: ByteArray?, key: SecretKey?): String? {
            try {
                val cipher = Cipher.getInstance("AES")
                val keySpec = SecretKeySpec(key?.encoded, "AES")
                cipher.init(Cipher.DECRYPT_MODE, keySpec)
                val decryptedText = cipher.doFinal(cipherText)
                return String(decryptedText)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun encrypt(
            plaintext: ByteArray?,
            key: SecretKey?,
        ): ByteArray? {
            val cipher = Cipher.getInstance("AES")
            val keySpec = SecretKeySpec(key?.encoded, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            return cipher.doFinal(plaintext)
        }

        fun generateSecretKeyCipher(): SecretKey? {
            var keyGenerator: KeyGenerator? = null
            var secretKey: SecretKey? = null
            try {
                keyGenerator = KeyGenerator.getInstance("AES")
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            keyGenerator?.init(256)
            secretKey = keyGenerator?.generateKey()
            return secretKey
        }

        fun encoderSecretKey(decval: ByteArray?): String? {
            return Base64.encodeToString(decval, Base64.DEFAULT)
        }

        fun decoderSecretKey(enval: String?): ByteArray {
            return Base64.decode(enval, Base64.DEFAULT)
        }

        fun strSecretKey(secretKey: SecretKey?): String? {
            val secretKeyen = secretKey?.encoded
            val strSecretKey = encoderSecretKey(secretKeyen)
            return strSecretKey
        }

        fun getSecretKey(secretKey: String?): SecretKey? {
            val encodedSecretKey: ByteArray = decoderSecretKey(secretKey)
            val originalSecretKey: SecretKey =
                SecretKeySpec(encodedSecretKey, 0, encodedSecretKey.size, "AES")

            return originalSecretKey
        }
    }
}
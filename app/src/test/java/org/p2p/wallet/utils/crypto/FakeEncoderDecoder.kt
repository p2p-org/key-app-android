package org.p2p.wallet.utils.crypto

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import io.mockk.every
import io.mockk.mockk
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import org.p2p.wallet.common.crypto.keystore.EncoderDecoder
import org.p2p.wallet.utils.generateRandomBytes

class FakeEncoderDecoder(private val keyStore: KeyStore) : EncoderDecoder {
    private val keyGenerator: KeyGenerator = KeyGenerator.getInstance("AES", keyStore.type)
    private val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    private val keyToIv = mutableMapOf<String, IvParameterSpec>()

    private fun createParameterSpec(keyAlias: String): KeyGenParameterSpec = mockk(relaxed = true) {
        every { keystoreAlias }.returns(keyAlias)
    }

    override fun init(keyStore: KeyStore, providerName: String, preferences: SharedPreferences) {
    }

    override fun encode(keyAlias: String, data: ByteArray): ByteArray {
        val key: Key = if (keyStore.containsAlias(keyAlias)) {
            keyStore.getKey(keyAlias, null)
        } else {
            keyGenerator.init(createParameterSpec(keyAlias))
            keyGenerator.generateKey()
        }
        val iv = IvParameterSpec(generateRandomBytes(16)).also {
            keyToIv[keyAlias] = it
        }

        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        return cipher.doFinal(data)
    }

    override fun decode(keyAlias: String, encodedData: ByteArray): ByteArray {
        val key: Key = keyStore.getKey(keyAlias, null)!!
        cipher.init(Cipher.DECRYPT_MODE, key, keyToIv[keyAlias]!!)
        return cipher.doFinal(encodedData)
    }

    override fun createSecuredKey(keyAlias: String) {
        keyGenerator.init(createParameterSpec(keyAlias))
        keyGenerator.generateKey()
    }

    override fun getCipher(keyAlias: String, cipherMode: Int): Cipher {
        val exposedCipher = Cipher.getInstance("AES")

        val key = keyStore.getKey(keyAlias, null)
        val ivSpec = keyToIv[keyAlias]

        exposedCipher.init(cipherMode, key, ivSpec)
        return exposedCipher
    }

    override fun onKeyDeleted(keyAlias: String) {
        keyStore.deleteEntry(keyAlias)
    }
}

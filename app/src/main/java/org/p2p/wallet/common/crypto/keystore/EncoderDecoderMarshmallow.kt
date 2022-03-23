@file:TargetApi(Build.VERSION_CODES.M)

package org.p2p.wallet.common.crypto.keystore

import android.annotation.TargetApi
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import org.p2p.solanaj.utils.crypto.Hex
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

private const val KEY_PARAMETER_SPEC_PURPOSES = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
private const val TRANSFORMATION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
private const val TRANSFORMATION_BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
private const val TRANSFORMATION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
private const val TRANSFORMATION = "$TRANSFORMATION_ALGORITHM/$TRANSFORMATION_BLOCK_MODE/$TRANSFORMATION_PADDING"
private const val AES_KEY_SIZE = 256
private const val PREF_INITIALIZATION_VECTOR_PREFIX = "PREF_KEY_INITIALIZATION_VECTOR"

class EncoderDecoderMarshmallow(
    private val sharedPreferences: SharedPreferences
) : EncoderDecoder {

    private lateinit var keyStore: KeyStore
    private lateinit var providerName: String

    private val cipher by lazy { Cipher.getInstance(TRANSFORMATION) }
    private val randomSecureRandom = SecureRandom()

    override fun setKeyStore(keyStore: KeyStore, providerName: String) {
        this.keyStore = keyStore
        this.providerName = providerName
    }

    @Synchronized
    override fun encode(keyAlias: String, data: ByteArray): ByteArray {
        val key = if (keyStore.containsAlias(keyAlias)) {
            keyStore.getKey(keyAlias, null)
        } else {
            generateKey(keyAlias, false)
        }

        val ivSpec = getIvSpec(cipher, keyAlias)

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)

        return cipher.doFinal(data)
    }

    @Synchronized
    override fun decode(keyAlias: String, encodedData: ByteArray): ByteArray {
        val key = keyStore.getKey(keyAlias, null)
            ?: throw IllegalArgumentException("Keystore contains no key for alias $keyAlias")

        val ivSpec = getIvSpec(cipher, keyAlias)

        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

        return cipher.doFinal(encodedData)
    }

    override fun onKeyDeleted(keyAlias: String) {
        sharedPreferences.edit { remove(getIvPrefKey(keyAlias)) }
    }

    @Synchronized
    override fun createSecuredKey(keyAlias: String) {
        if (keyStore.containsAlias(keyAlias)) {
            throw IllegalStateException("Key store already contains key for alias $keyAlias")
        }
        generateKey(keyAlias, false)
    }

    override fun getCipher(keyAlias: String, cipherMode: Int): Cipher {
        if (!keyStore.containsAlias(keyAlias)) {
            throw IllegalStateException("Key store contains no key for alias $keyAlias")
        }

        val exposedCipher = Cipher.getInstance(TRANSFORMATION)

        val key = keyStore.getKey(keyAlias, null)
        val ivSpec = getIvSpec(exposedCipher, keyAlias)

        exposedCipher.init(cipherMode, key, ivSpec)

        return exposedCipher
    }

    private fun generateKey(keyAlias: String, isSecured: Boolean): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(TRANSFORMATION_ALGORITHM, providerName)
        val spec = KeyGenParameterSpec.Builder(keyAlias, KEY_PARAMETER_SPEC_PURPOSES)
            .setKeySize(AES_KEY_SIZE)
            .setBlockModes(TRANSFORMATION_BLOCK_MODE)
            .setEncryptionPaddings(TRANSFORMATION_PADDING)
            .setUserAuthenticationRequired(isSecured)
            .setRandomizedEncryptionRequired(false)
            .build()
        keyGenerator.init(spec)

        return keyGenerator.generateKey()
    }

    private fun getIvSpec(cipher: Cipher, keyAlias: String): IvParameterSpec {
        val ivPrefKey = getIvPrefKey(keyAlias)
        val savedIv = sharedPreferences.getString(ivPrefKey, null)?.let { Hex.decode(it) }

        return if (savedIv == null) {
            val iv = ByteArray(cipher.blockSize)
            randomSecureRandom.nextBytes(iv)
            sharedPreferences.edit { putString(ivPrefKey, Hex.encode(iv)) }
            IvParameterSpec(iv)
        } else {
            IvParameterSpec(savedIv)
        }
    }

    private fun getIvPrefKey(keyAlias: String) = "$keyAlias\\_$PREF_INITIALIZATION_VECTOR_PREFIX"
}

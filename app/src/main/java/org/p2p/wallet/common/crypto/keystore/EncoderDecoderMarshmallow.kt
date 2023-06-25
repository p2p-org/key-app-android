package org.p2p.wallet.common.crypto.keystore

import androidx.core.content.edit
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.p2p.core.crypto.Hex
import timber.log.Timber
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
private const val TAG = "EncoderDecoderMarshmallow"

class EncoderDecoderMarshmallow : EncoderDecoder {

    private lateinit var keyStore: KeyStore
    private lateinit var providerName: String
    private lateinit var sharedPreferences: SharedPreferences

    private val cipher by lazy { Cipher.getInstance(TRANSFORMATION) }
    private val randomSecureRandom = SecureRandom()

    private val availableAliases: List<String>
        get() = keyStore.aliases().toList()

    override fun init(keyStore: KeyStore, providerName: String, preferences: SharedPreferences) {
        this.keyStore = keyStore
        this.providerName = providerName
        this.sharedPreferences = preferences

        Timber.tag(TAG).i("init called with ($keyStore;$providerName;$preferences)")
        Timber.tag(TAG).i("init keystore aliases: ${keyStore.aliases().toList()}")
    }

    @Synchronized
    override fun encode(keyAlias: String, data: ByteArray): ByteArray {
        Timber.tag(TAG).i("!-- encode($keyAlias) begin")
        val key = if (keyStore.containsAlias(keyAlias)) {
            Timber.tag(TAG).i("encode($keyAlias) containsAlias = true")
            keyStore.getKey(keyAlias, null)
        } else {
            Timber.tag(TAG).i("encode($keyAlias) containsAlias = false; generating key")
            generateKey(keyAlias, false)
        }

        val ivSpec = getIvSpec(cipher, keyAlias)

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)

        return cipher.doFinal(data)
            .also { Timber.tag(TAG).i("!-- encode($keyAlias) finish") }
    }

    @Throws(IllegalArgumentException::class)
    @Synchronized
    override fun decode(keyAlias: String, encodedData: ByteArray): ByteArray {
        Timber.tag(TAG).i("!-- decode($keyAlias) begin")
        val key = keyStore.getKey(keyAlias, null)

        if (key == null) {
            Timber.tag(TAG).i("decode($keyAlias) key == null")
            Timber.tag(TAG).i("decode($keyAlias) available aliases: $availableAliases")
            Timber.tag(TAG).i("!-- decode($keyAlias) finish with ERROR")
            throw IllegalArgumentException(
                "Keystore contains no key for alias $keyAlias; available aliases: $availableAliases"
            )
        }

        val ivSpec = getIvSpec(cipher, keyAlias)

        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

        return cipher.doFinal(encodedData)
            .also { Timber.tag(TAG).i("!-- decode($keyAlias) finish") }
    }

    override fun onKeyDeleted(keyAlias: String) {
        sharedPreferences.edit { remove(getIvPrefKey(keyAlias)) }
            .also { Timber.tag(TAG).i("!-- onKeyDeleted($keyAlias) finish") }
    }

    @Throws(IllegalStateException::class)
    @Synchronized
    override fun createSecuredKey(keyAlias: String) {
        Timber.tag(TAG).i("!-- createSecuredKey($keyAlias) begin")
        if (keyStore.containsAlias(keyAlias)) {
            throw IllegalStateException(
                "Key store already contains key for alias $keyAlias; available aliases: $availableAliases"
            )
        }
        generateKey(keyAlias, false)
        Timber.tag(TAG).i("!-- createSecuredKey($keyAlias) finish")
    }

    @Throws(IllegalStateException::class)
    override fun getCipher(keyAlias: String, cipherMode: Int): Cipher {
        if (!keyStore.containsAlias(keyAlias)) {
            throw IllegalStateException(
                "Key store contains no key for alias $keyAlias; available aliases: $availableAliases"
            )
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
            Timber.tag(TAG).i("getIvSpec($keyAlias) savedIv == null; generating new one")
            val iv = ByteArray(cipher.blockSize)
            randomSecureRandom.nextBytes(iv)
            sharedPreferences.edit { putString(ivPrefKey, Hex.encode(iv)) }
            IvParameterSpec(iv)
        } else {
            Timber.tag(TAG).i("getIvSpec($keyAlias) savedIv != null")
            IvParameterSpec(savedIv)
        }
    }

    private fun getIvPrefKey(keyAlias: String): String = "$keyAlias\\_$PREF_INITIALIZATION_VECTOR_PREFIX"
}

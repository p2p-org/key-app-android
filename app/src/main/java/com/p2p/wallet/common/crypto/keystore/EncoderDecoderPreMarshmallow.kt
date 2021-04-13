@file:Suppress("DEPRECATION")

package com.p2p.wallet.common.crypto.keystore

import android.content.Context
import android.security.KeyPairGeneratorSpec
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.AlgorithmParameterSpec
import java.util.Calendar
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal

private const val CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
private const val KEY_EXPIRATION_YEARS = 25
private const val RSA_KEY_SIZE = 4096

class EncoderDecoderPreMarshmallow(private val context: Context) : EncoderDecoder {

    private lateinit var keyStore: KeyStore
    private lateinit var providerName: String

    private val cipher by lazy { Cipher.getInstance(CIPHER_TRANSFORMATION) }

    override fun setKeyStore(keyStore: KeyStore, providerName: String) {
        this.keyStore = keyStore
        this.providerName = providerName
    }

    @Synchronized
    override fun encode(keyAlias: String, data: ByteArray): ByteArray {
        val publicKey = if (keyStore.containsAlias(keyAlias)) {
            keyStore.getCertificate(keyAlias).publicKey
        } else {
            generatePublicKey(keyAlias, false).public
        }

        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        return cipher.doFinal(data)
    }

    @Synchronized
    override fun decode(keyAlias: String, encodedData: ByteArray): ByteArray {
        val key = keyStore.getKey(keyAlias, null)
            ?: throw IllegalArgumentException("Keystore contains no key for alias $keyAlias")

        cipher.init(Cipher.DECRYPT_MODE, key)

        return cipher.doFinal(encodedData)
    }

    @Synchronized
    override fun createSecuredKey(keyAlias: String) {
        if (keyStore.containsAlias(keyAlias)) {
            throw IllegalStateException("Key store already contains key for alias $keyAlias")
        }
        generatePublicKey(keyAlias, true)
    }

    override fun getCipher(keyAlias: String, cipherMode: Int): Cipher {
        if (!keyStore.containsAlias(keyAlias)) {
            throw IllegalStateException("Key store contains no key for alias $keyAlias")
        }

        val key = when (cipherMode) {
            Cipher.ENCRYPT_MODE -> keyStore.getCertificate(keyAlias).publicKey
            Cipher.DECRYPT_MODE -> keyStore.getKey(keyAlias, null)
            else -> throw IllegalStateException("Unexpected cipher mode $cipherMode")
        }

        val exposedCipher = Cipher.getInstance(CIPHER_TRANSFORMATION)

        exposedCipher.init(cipherMode, key)

        return exposedCipher
    }

    override fun onKeyDeleted(keyAlias: String) {
        // Do nothing here.
    }

    private fun generatePublicKey(keyAlias: String, isSecured: Boolean): KeyPair {
        val keyGenerator = KeyPairGenerator.getInstance("RSA", providerName)
        keyGenerator.initialize(getAlgorithmParameters(keyAlias, isSecured))
        return keyGenerator.generateKeyPair()
    }

    private fun getAlgorithmParameters(keyAlias: String, isSecured: Boolean): AlgorithmParameterSpec {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.YEAR, KEY_EXPIRATION_YEARS)
        @Suppress("DEPRECATION") // Since it is used for pre-marshmallow devices.
        return KeyPairGeneratorSpec.Builder(context)
            .setAlias(keyAlias)
            .setKeySize(RSA_KEY_SIZE)
            .setSubject(X500Principal("CN=$keyAlias, OU=${context.packageName}"))
            .setSerialNumber(BigInteger.ONE)
            .setStartDate(startDate.time)
            .setEndDate(endDate.time)
            .apply { if (isSecured) setEncryptionRequired() }
            .build()
    }
}
package org.p2p.wallet.utils.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidAlgorithmParameterException
import java.security.Key
import java.security.KeyStore
import java.security.KeyStoreSpi
import java.security.PrivateKey
import java.security.Provider
import java.security.SecureRandom
import java.security.Security
import java.security.cert.Certificate
import java.security.spec.AlgorithmParameterSpec
import java.util.Collections
import java.util.Date
import java.util.Enumeration
import javax.crypto.KeyGenerator
import javax.crypto.KeyGeneratorSpi
import javax.crypto.SecretKey

internal class FakeAndroidKeyStoreProvider : Provider(
    /* name = */ "AndroidKeyStore",
    /* version = */ 1.0,
    /* info = */ "Fake AndroidKeyStore provider"
) {
    companion object {
        private var isSetupCalled: Boolean = false

        const val FAKE_KEYSTORE_NAME = "AndroidKeyStore"

        fun setup() {
            Security.addProvider(FakeAndroidKeyStoreProvider())
            isSetupCalled = true
        }

        fun getKeyStore(): KeyStore {
            setup()
            require(isSetupCalled) { "setup() was not called before getting keystore" }
            return KeyStore.getInstance(FAKE_KEYSTORE_NAME)
                .apply { load(null) }
        }
    }

    init {
        put(
            "KeyStore.AndroidKeyStore",
            FakeKeyStore::class.java.name
        )
        put(
            "KeyGenerator.AES",
            FakeKeyGenerator::class.java.name
        )
    }
}

internal class FakeKeyStore : KeyStoreSpi() {
    companion object {
        private val keys = mutableMapOf<String, Key>()
        private val certs = mutableMapOf<String, Certificate>()
    }

    override fun engineIsKeyEntry(alias: String?): Boolean {
        requireNotNull(alias) { "alias == null" }

        return keys.containsKey(alias)
    }

    override fun engineIsCertificateEntry(alias: String?): Boolean {
        requireNotNull(alias) { "alias == null" }

        return certs.containsKey(alias)
    }

    override fun engineGetCertificate(alias: String?): Certificate {
        requireNotNull(alias) { "alias == null" }

        return certs.getValue(alias)
    }

    override fun engineGetCreationDate(alias: String?): Date {
        requireNotNull(alias) { "alias == null" }

        return Date()
    }

    override fun engineDeleteEntry(alias: String?) {
        requireNotNull(alias) { "alias == null" }

        keys.remove(alias)
        certs.remove(alias)
    }

    override fun engineSetKeyEntry(
        alias: String?,
        key: Key?,
        password: CharArray?,
        chain: Array<out Certificate>?
    ) {
        requireNotNull(alias) { "alias == null" }
        requireNotNull(key) { "key == null" }

        keys[alias] = key
    }

    override fun engineGetEntry(
        alias: String?,
        protParam: KeyStore.ProtectionParameter?
    ): KeyStore.Entry {
        requireNotNull(alias) { "alias == null" }

        val key = keys[alias]
        if (key != null) {
            return when (key) {
                is SecretKey -> KeyStore.SecretKeyEntry(key)
                is PrivateKey -> KeyStore.PrivateKeyEntry(key, null)
                else -> throw UnsupportedOperationException("Unsupported key type: $key")
            }
        }
        val cert = certs[alias]
        if (cert != null) {
            return KeyStore.TrustedCertificateEntry(cert)
        }
        throw UnsupportedOperationException("No alias found in keys or certs, alias=$alias")
    }

    override fun engineSetKeyEntry(alias: String?, key: ByteArray?, chain: Array<out Certificate>?) =
        throw UnsupportedOperationException("Operation not supported because key encoding is unknown")

    override fun engineStore(stream: OutputStream?, password: CharArray?) =
        throw UnsupportedOperationException("Can not serialize AndroidKeyStore to OutputStream")

    override fun engineSize(): Int {
        val uniqueAlias = mutableSetOf<String>().apply {
            addAll(keys.keys)
            addAll(certs.keys)
        }
        return uniqueAlias.size
    }

    override fun engineAliases(): Enumeration<String> {
        val uniqueAlias = mutableSetOf<String>().apply {
            addAll(keys.keys)
            addAll(certs.keys)
        }
        return Collections.enumeration(uniqueAlias)
    }

    override fun engineContainsAlias(alias: String?): Boolean {
        requireNotNull(alias) { "alias == null" }

        return keys.containsKey(alias) || certs.containsKey(alias)
    }

    override fun engineLoad(stream: InputStream?, password: CharArray?) {
        require(stream == null) { "InputStream not supported" }
        require(password == null) { "password not supported" }

        // Do nothing in this fake key store.
    }

    override fun engineGetCertificateChain(alias: String?): Array<Certificate> {
        requireNotNull(alias) { "alias == null" }

        val cert = certs[alias] ?: return arrayOf()
        return arrayOf(cert)
    }

    override fun engineSetCertificateEntry(alias: String?, cert: Certificate?) {
        requireNotNull(alias) { "alias == null" }
        requireNotNull(cert) { "cert == null" }

        certs[alias] = cert
    }

    override fun engineGetCertificateAlias(cert: Certificate?): String? {
        requireNotNull(cert) { "cert == null" }

        for (entry in certs.entries) {
            if (entry.value == cert) {
                return entry.key
            }
        }
        return null
    }

    override fun engineGetKey(alias: String?, password: CharArray?): Key? {
        return keys[alias]!!
    }
}

internal class FakeKeyGenerator : KeyGeneratorSpi() {
    private val wrapped = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
    private var keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private var spec: KeyGenParameterSpec? = null

    override fun engineInit(random: SecureRandom) {
        throw UnsupportedOperationException(
            "Cannot initialize without a ${KeyGenParameterSpec::class.java.name} parameter"
        )
    }

    override fun engineInit(params: AlgorithmParameterSpec?, random: SecureRandom) {
        if (params == null || params !is KeyGenParameterSpec) {
            throw InvalidAlgorithmParameterException(
                "Cannot initialize without a ${KeyGenParameterSpec::class.java.name} parameter"
            )
        }
        spec = params
    }

    override fun engineInit(keysize: Int, random: SecureRandom?) {
        throw UnsupportedOperationException(
            "Cannot initialize without a ${KeyGenParameterSpec::class.java.name} parameter"
        )
    }

    override fun engineGenerateKey(): SecretKey {
        val spec = spec ?: throw IllegalStateException("Not initialized")

        val secretKey = wrapped.generateKey()
        keyStore.setKeyEntry(
            spec.keystoreAlias,
            secretKey,
            null,
            null
        )
        return secretKey
    }
}

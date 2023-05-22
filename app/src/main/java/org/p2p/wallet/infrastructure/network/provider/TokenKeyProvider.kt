package org.p2p.wallet.infrastructure.network.provider

import timber.log.Timber
import kotlinx.coroutines.runBlocking
import org.p2p.solanaj.utils.crypto.Base58Utils
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.infrastructure.security.SecureStorageContract.Key
import org.p2p.wallet.utils.Base58String
import org.p2p.wallet.utils.emptyString
import org.p2p.wallet.utils.toBase58Instance

private const val TAG = "TokenKeyProvider"

class TokenKeyProvider(
    private val secureStorage: SecureStorageContract
) {

    fun interface TokenKeyProviderListener {
        fun onPublicKeyChanged(newPublicKey: String)
    }

    private val listeners = mutableListOf<TokenKeyProviderListener>()

    var useStubKey: Boolean
        set(value) = secureStorage.saveString(Key.KEY_USE_STUB_PUBLIC_KEY, value.toString())
        get() = BuildConfig.DEBUG && secureStorage.getString(Key.KEY_USE_STUB_PUBLIC_KEY)?.toBoolean() ?: false

    var publicKey: String = emptyString()
        get() = getPublicKeyFromStorage()
        set(value) {
            field = value
            savePublicKeyToStorage(value)
            Timber.tag(TAG).i("updating user public key: $value")
        }

    val publicKeyBase58: Base58String
        get() = publicKey.toBase58Instance()

    private fun getPublicKeyFromStorage(): String = runBlocking {
        try {
            val keyName = if (useStubKey) Key.KEY_STUB_PUBLIC_KEY else Key.KEY_PUBLIC_KEY
            val base58String = secureStorage.getString(keyName).orEmpty()
            Base58Utils.decodeToString(base58String)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
            throw e
        }
    }
        .also { Timber.tag(TAG).i("getting user public key: $it; mocked=$useStubKey") }

    private fun savePublicKeyToStorage(value: String) {
        runBlocking {
            val result = Base58Utils.encodeFromString(value)
            val keyName = if (useStubKey) Key.KEY_STUB_PUBLIC_KEY else Key.KEY_PUBLIC_KEY
            secureStorage.saveString(keyName, result)

            listeners.forEach { it.onPublicKeyChanged(value) }
        }
    }

    var keyPair: ByteArray = ByteArray(0)
        get() = getSecretKeyFromStorage()
        set(value) {
            field = value
            saveSecretKeyToStorage(value)

            Timber.tag(TAG).i("updating user secret key: ${value.size}")
        }

    private fun getSecretKeyFromStorage(): ByteArray {
        val result = secureStorage.getString(Key.KEY_SECRET_KEY).orEmpty()
        return Base58Utils.decode(result)
            .also { Timber.tag(TAG).i("getting user secret key: ${it.size}") }
    }

    private fun saveSecretKeyToStorage(value: ByteArray) {
        runBlocking {
            val result = Base58Utils.encode(value.copyOf())
            secureStorage.saveString(Key.KEY_SECRET_KEY, result)
        }
    }

    fun registerListener(listener: TokenKeyProviderListener) {
        listeners += listener
    }

    fun clear() {
        publicKey = emptyString()
        keyPair = byteArrayOf()
    }
}

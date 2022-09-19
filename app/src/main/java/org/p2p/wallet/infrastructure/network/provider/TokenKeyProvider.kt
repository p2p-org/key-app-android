package org.p2p.wallet.infrastructure.network.provider

import org.p2p.solanaj.utils.crypto.Base58Utils
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.utils.emptyString
import timber.log.Timber
import kotlinx.coroutines.runBlocking

private const val KEY_PUBLIC_KEY = "KEY_PUBLIC_KEY"
private const val KEY_SECRET_KEY = "KEY_SECRET_KEY"

private const val TAG = "TokenKeyProvider"

class TokenKeyProvider(
    private val secureStorage: SecureStorageContract,
) {

    fun interface TokenKeyProviderListener {
        fun onPublicKeyChanged(newPublicKey: String)
    }

    private val listeners = mutableListOf<TokenKeyProviderListener>()

    var publicKey: String = getPublicKeyFromStorage()
        set(value) {
            field = value
            savePublicKeyToStorage(value)
            Timber.tag(TAG).i("updating user public key: $value")
        }

    private fun getPublicKeyFromStorage(): String {
        return runBlocking {
            val base58String = secureStorage.getString(KEY_PUBLIC_KEY).orEmpty()
            Base58Utils.decodeToString(base58String)
        }
            .also { Timber.tag(TAG).i("getting user public key: $it") }
    }

    private fun savePublicKeyToStorage(value: String) {
        runBlocking {
            val result = Base58Utils.encodeFromString(value)
            secureStorage.saveString(KEY_PUBLIC_KEY, result)

            listeners.forEach { it.onPublicKeyChanged(value) }
        }
    }

    var secretKey: ByteArray = getSecretKeyFromStorage()
        set(value) {
            field = value
            saveSecretKeyToStorage(value)

            Timber.tag(TAG).i("updating user secret key: ${value.size}")
        }

    private fun getSecretKeyFromStorage(): ByteArray {
        val result = secureStorage.getString(KEY_SECRET_KEY).orEmpty()
        return Base58Utils.decode(result)
            .also { Timber.tag(TAG).i("getting user secret key: ${it.size}") }
    }

    private fun saveSecretKeyToStorage(value: ByteArray) {
        runBlocking {
            val result = Base58Utils.encode(value.copyOf())
            secureStorage.saveString(KEY_SECRET_KEY, result)
        }
    }

    fun registerListener(listener: TokenKeyProviderListener) {
        listeners += listener
    }

    fun clear() {
        publicKey = emptyString()
        secretKey = byteArrayOf()
    }
}

package org.p2p.wallet.infrastructure.network.provider

import kotlinx.coroutines.runBlocking
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.infrastructure.security.SecureStorageContract.Key
import timber.log.Timber

private const val TAG = "SeedPhraseProvider"

class SeedPhraseProvider(
    private val secureStorage: SecureStorageContract
) {

    var seedPhrase: List<String> = emptyList()
        get() = getSeedPhraseFromStorage()
        set(value) {
            field = value
            saveSeedPhraseToStorage(value)
            Timber.tag(TAG).i("updating user public key: $value")
        }

    private fun getSeedPhraseFromStorage(): List<String> = runBlocking {
        try {
            secureStorage.getObjectList(Key.KEY_SEED_PHRASE)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
            throw e
        }
    }

    private fun saveSeedPhraseToStorage(value: List<String>) {
        runBlocking {
            secureStorage.saveObjectList(Key.KEY_SEED_PHRASE, value)
        }
    }

    fun clear() {
        seedPhrase = emptyList()
    }
}

package org.p2p.wallet.infrastructure.network.provider

import kotlinx.coroutines.runBlocking
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.infrastructure.security.SecureStorageContract.Key
import timber.log.Timber

private const val TAG = "SeedPhraseProvider"

class SeedPhraseProvider(
    private val secureStorage: SecureStorageContract
) {

    val isAvailable: Boolean
        get() = getUserSeedPhrase().seedPhrase.isNotEmpty()

    fun getUserSeedPhrase(): UserSeedPhrase {
        return UserSeedPhrase(
            seedPhrase = seedPhrase,
            provider = seedPhraseProvider
        )
    }

    fun updateUserSeedPhrase(words: List<String>, provider: SeedPhraseProviderType) {
        seedPhrase = words
        seedPhraseProvider = provider
    }

    private var seedPhrase: List<String> = emptyList()
        get() = getSeedPhraseFromStorage()
        set(value) {
            field = value
            saveSeedPhraseToStorage(value)
            Timber.tag(TAG).i("updating user public key: $value")
        }

    private var seedPhraseProvider: SeedPhraseProviderType? = null
        get() = getSeedPhraseProviderFromStorage()
        set(value) {
            field = value
            value?.let { saveSeedPhraseProviderToStorage(it) }
            Timber.tag(TAG).i("updating user public key: $value")
        }

    private fun getSeedPhraseFromStorage(): List<String> =
        try {
            val result: List<String> = secureStorage.getObjectList(Key.KEY_SEED_PHRASE)
            result
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
            throw e
        }

    private fun getSeedPhraseProviderFromStorage(): SeedPhraseProviderType? =
        try {
            val result: String? = secureStorage.getString(Key.KEY_SEED_PHRASE_PROVIDER)
            SeedPhraseProviderType.getValueOf(result)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
            throw e
        }

    private fun saveSeedPhraseToStorage(value: List<String>) {
        runBlocking {
            secureStorage.saveObjectList(Key.KEY_SEED_PHRASE, value)
        }
    }

    private fun saveSeedPhraseProviderToStorage(value: SeedPhraseProviderType) {
        runBlocking {
            secureStorage.saveString(Key.KEY_SEED_PHRASE_PROVIDER, value.title)
        }
    }

    fun clear() {
        seedPhrase = emptyList()
    }
}

enum class SeedPhraseProviderType(val title: String) {
    MANUAL("Manual"),
    WEB_AUTH("Web3Auth");

    companion object {
        fun getValueOf(value: String?) = values().firstOrNull {
            it.title == value
        }
    }
}

data class UserSeedPhrase(
    val seedPhrase: List<String>,
    val provider: SeedPhraseProviderType?
)

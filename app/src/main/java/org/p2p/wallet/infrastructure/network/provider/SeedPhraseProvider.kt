package org.p2p.wallet.infrastructure.network.provider

import timber.log.Timber
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.infrastructure.security.SecureStorageContract.Key

private const val TAG = "SeedPhraseProvider"

class SeedPhraseProvider(
    private val secureStorage: SecureStorageContract
) {

    val isAvailable: Boolean
        get() = getUserSeedPhrase().seedPhrase.isNotEmpty()

    fun getUserSeedPhrase(): UserSeedPhraseDetails {
        return UserSeedPhraseDetails(
            seedPhrase = seedPhrase,
            provider = seedPhraseProvider
        )
    }

    fun setUserSeedPhrase(words: List<String>, provider: SeedPhraseSource) {
        seedPhrase = words
        seedPhraseProvider = provider
    }

    private var seedPhrase: List<String> = emptyList()
        get() = getSeedPhraseFromStorage()
        set(value) {
            field = value
            saveSeedPhraseToStorage(value)
            Timber.tag(TAG).i("updating user seed phrase: isEmpty=${value.isEmpty()}")
        }

    private var seedPhraseProvider: SeedPhraseSource = SeedPhraseSource.NOT_PROVIDED
        get() = getSeedPhraseProviderFromStorage()
        set(value) {
            field = value
            saveSeedPhraseProviderToStorage(value)
            Timber.tag(TAG).i("updating user seed phrase provider: $value")
        }

    private fun getSeedPhraseFromStorage(): List<String> =
        try {
            val result: List<String> = secureStorage.getObjectList(Key.KEY_SEED_PHRASE)
            result
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
            throw e
        }

    private fun getSeedPhraseProviderFromStorage(): SeedPhraseSource =
        try {
            val result: String? = secureStorage.getString(Key.KEY_SEED_PHRASE_PROVIDER)
            SeedPhraseSource.getValueOf(result)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
            throw e
        }

    private fun saveSeedPhraseToStorage(value: List<String>) {
        secureStorage.saveObjectList(Key.KEY_SEED_PHRASE, value)
    }

    private fun saveSeedPhraseProviderToStorage(value: SeedPhraseSource) {
        secureStorage.saveString(Key.KEY_SEED_PHRASE_PROVIDER, value.title)
    }

    fun clear() {
        seedPhrase = emptyList()
    }
}

data class UserSeedPhraseDetails(
    val seedPhrase: List<String>,
    val provider: SeedPhraseSource
)

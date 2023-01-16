package org.p2p.wallet.infrastructure.network.provider

import org.p2p.wallet.infrastructure.security.SecureStorageContract
import org.p2p.wallet.infrastructure.security.SecureStorageContract.Key
import org.p2p.wallet.send.model.CurrencyMode
import org.p2p.wallet.utils.emptyString
import timber.log.Timber

private const val TAG = "SendModeProvider"

private val EMPTY_TOKEN = CurrencyMode.Token(emptyString(), 2)

class SendModeProvider(
    private val secureStorage: SecureStorageContract
) {

    var sendMode: CurrencyMode = EMPTY_TOKEN
        get() = getSendModeFromStorage()
        set(value) {
            field = value
            saveSendModeToStorage(value)
            Timber.tag(TAG).i("updating user seed phrase: $value")
        }

    private fun getSendModeFromStorage(): CurrencyMode =
        try {
            val result: CurrencyMode = secureStorage.getObject(Key.KEY_SEND_MODE, CurrencyMode::class) ?: EMPTY_TOKEN
            result
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
            throw e
        }

    private fun saveSendModeToStorage(value: CurrencyMode) {
        secureStorage.saveObject(Key.KEY_SEND_MODE, value)
    }

    fun clear() {
        sendMode = EMPTY_TOKEN
    }
}

package org.p2p.wallet.infrastructure.network.provider

import com.google.gson.annotations.SerializedName
import org.p2p.core.utils.Constants
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

    var sendMode: CurrencyMode = CurrencyMode.Usd
        get() = getSendModeFromStorage()
        set(value) {
            field = value
            saveSendModeToStorage(value)
            Timber.tag(TAG).i("updating user seed phrase: $value")
        }

    private fun getSendModeFromStorage(): CurrencyMode =
        try {
            val sendMode: SendMode = secureStorage.getObject(Key.KEY_SEND_MODE, SendMode::class)
                ?: SendMode(emptyString())
            if (sendMode.mode == Constants.USD_READABLE_SYMBOL) CurrencyMode.Usd
            else EMPTY_TOKEN
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
            throw e
        }

    private fun saveSendModeToStorage(value: CurrencyMode) {
        secureStorage.saveObject(
            Key.KEY_SEND_MODE,
            SendMode(if (value == CurrencyMode.Usd) Constants.USD_READABLE_SYMBOL else emptyString())
        )
    }

    fun clear() {
        sendMode = CurrencyMode.Usd
    }

    data class SendMode(
        @SerializedName("mode")
        val mode: String
    )
}

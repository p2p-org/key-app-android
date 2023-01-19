package org.p2p.wallet.infrastructure.network.provider

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.core.model.CurrencyMode
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.wallet.utils.emptyString
import timber.log.Timber

private const val TAG = "SendModeProvider"
private const val KEY_SEND_MODE = "SEND_MODE"

private val EMPTY_TOKEN = CurrencyMode.Token(emptyString(), 2)

class SendModeProvider(
    private val sharedPreferences: SharedPreferences
) {

    var sendMode: CurrencyMode = CurrencyMode.Fiat.Usd
        get() = getSendModeFromStorage()
        set(value) {
            field = value
            saveSendModeToStorage(value)
        }

    private fun getSendModeFromStorage(): CurrencyMode =
        try {
            val sendMode: String = sharedPreferences.getString(KEY_SEND_MODE, USD_READABLE_SYMBOL)
                ?: USD_READABLE_SYMBOL
            if (sendMode == USD_READABLE_SYMBOL) {
                CurrencyMode.Fiat.Usd
            } else {
                EMPTY_TOKEN
            }
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
            throw e
        }

    private fun saveSendModeToStorage(value: CurrencyMode) {
        sharedPreferences.edit {
            putString(
                KEY_SEND_MODE,
                if (value == CurrencyMode.Fiat.Usd) {
                    USD_READABLE_SYMBOL
                } else {
                    emptyString()
                }
            )
        }
    }

    fun clear() {
        sendMode = CurrencyMode.Fiat.Usd
    }
}

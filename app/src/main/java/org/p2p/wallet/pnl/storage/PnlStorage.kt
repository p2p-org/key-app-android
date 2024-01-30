package org.p2p.wallet.pnl.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import java.time.ZonedDateTime
import org.p2p.core.crypto.Base58String
import org.p2p.core.network.gson.gsonGenericMapType
import org.p2p.core.utils.fromJsonReified
import org.p2p.wallet.pnl.models.PnlTokenData

class PnlStorage(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
) : PnlStorageContract {
    private companion object {
        const val KEY_TOTAL = "TOTAL"
        const val KEY_TOKENS = "TOKENS"
        const val KEY_LAST_UPDATE_TIME = "LAST_UPDATE_TIME"
    }

    override var total: PnlTokenData
        get() {
            return sharedPreferences
                .getString(KEY_TOTAL, null)
                ?.let { gson.fromJsonReified<PnlTokenData>(it) }
                ?: PnlTokenData("0", "0")
        }
        set(value) {
            sharedPreferences
                .edit()
                .putString(KEY_TOTAL, gson.toJson(value))
                .apply()
        }
    override var tokens: Map<Base58String, PnlTokenData>
        get() {
            return sharedPreferences
                .getString(KEY_TOKENS, null)
                ?.let {
                    gson.fromJson<Map<Base58String, PnlTokenData>>(it, gsonGenericMapType<Base58String, PnlTokenData>())
                }
                .orEmpty()
        }
        set(value) {
            sharedPreferences.edit().putString(KEY_TOKENS, gson.toJson(value)).apply()
        }

    override var lastUpdateTime: ZonedDateTime?
        get() {
            val time = sharedPreferences.getString(KEY_LAST_UPDATE_TIME, null)
            return time?.let(ZonedDateTime::parse)
        }
        set(value) {
            sharedPreferences.edit().putString(KEY_LAST_UPDATE_TIME, value.toString()).apply()
        }

    init {
        sharedPreferences.edit().clear().apply()
    }
}

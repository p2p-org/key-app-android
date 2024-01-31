package org.p2p.wallet.pnl.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import java.time.ZonedDateTime
import org.p2p.core.crypto.Base58String
import org.p2p.core.network.gson.gsonGenericMapType
import org.p2p.core.utils.fromJsonReified
import org.p2p.wallet.pnl.models.PnlData
import org.p2p.wallet.pnl.models.PnlTokenData

class PnlStorage(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
) : PnlStorageContract {
    private companion object {
        const val KEY_TOTAL = "TOTAL"
        const val KEY_TOKENS = "TOKENS"
        const val KEY_LAST_UPDATE_TIME = "LAST_UPDATE_TIME"
        const val EXPIRE_TIME_MINUTES: Long = 5
    }

    override fun getTotalPnlData(): PnlTokenData {
        return sharedPreferences
            .getString(KEY_TOTAL, null)
            ?.let { gson.fromJsonReified<PnlTokenData>(it) }
            ?: PnlTokenData("0", "0")
    }

    override fun setTotalPnlData(pnlTokenData: PnlTokenData) {
        sharedPreferences
            .edit()
            .putString(KEY_TOTAL, gson.toJson(pnlTokenData))
            .apply()
    }

    override fun getTokensPnlData(): Map<Base58String, PnlTokenData> {
        return sharedPreferences
            .getString(KEY_TOKENS, null)
            ?.let {
                gson.fromJson<Map<Base58String, PnlTokenData>>(it, gsonGenericMapType<Base58String, PnlTokenData>())
            }
            .orEmpty()
    }

    override fun addTokensPnlData(data: Map<Base58String, PnlTokenData>) {
        val tokens = getTokensPnlData().toMutableMap()
        tokens += data
        sharedPreferences.edit().putString(KEY_TOKENS, gson.toJson(tokens)).apply()
    }

    override fun getLastUpdatedTime(): ZonedDateTime? {
        val time = sharedPreferences.getString(KEY_LAST_UPDATE_TIME, null)
        return time?.let(ZonedDateTime::parse)
    }

    override fun setLastUpdatedTime(time: ZonedDateTime) {
        sharedPreferences.edit().putString(KEY_LAST_UPDATE_TIME, time.toString()).apply()
    }

    override fun hasToken(mintAddress: Base58String): Boolean = getTokensPnlData().containsKey(mintAddress)

    override fun hasAllTokens(mintAddresses: List<Base58String>): Boolean {
        return getTokensPnlData().keys.containsAll(mintAddresses)
    }

    override fun isCacheExpired(): Boolean {
        return getLastUpdatedTime() == null || getLastUpdatedTime()!!
            .plusMinutes(EXPIRE_TIME_MINUTES)
            .isBefore(ZonedDateTime.now())
    }

    override suspend fun getOrCache(dataGetter: suspend () -> PnlData): PnlData {
        if (isCacheExpired()) {
            val data = dataGetter()
            setTotalPnlData(data.total)
            addTokensPnlData(data.tokens)
            setLastUpdatedTime(ZonedDateTime.now())
        }
        return PnlData(getTotalPnlData(), getTokensPnlData())
    }
}

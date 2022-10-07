package org.p2p.wallet.infrastructure.account

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import org.p2p.wallet.infrastructure.account.AccountStorageContract.Key
import kotlin.reflect.KClass

class AccountStorage(
    context: Context,
    private val gson: Gson
) : AccountStorageContract {

    private val prefsName: String = "${context.packageName}.account_prefs"
    private val sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    override fun <T> saveObject(key: Key, data: T) {
        val objectAsJson = gson.toJson(data)
        sharedPreferences.edit { putString(key.prefsValue, objectAsJson) }
    }

    @Throws(IllegalArgumentException::class)
    override fun <T : Any> getObject(key: Key, type: KClass<T>): T? {
        return sharedPreferences.getString(key.prefsValue, null)
            ?.let { gson.fromJson(it, type.java) }
    }

    override fun saveString(key: Key, data: String) {
        sharedPreferences.edit { putString(key.prefsValue, data) }
    }

    override fun getString(key: Key): String? {
        return sharedPreferences.getString(key.prefsValue, null)
    }

    override fun contains(key: Key): Boolean = sharedPreferences.contains(key.prefsValue)

    override fun remove(key: Key) {
        sharedPreferences.edit { remove(key.prefsValue) }
    }

    override fun removeAll() {
        sharedPreferences.edit { clear() }
    }
}

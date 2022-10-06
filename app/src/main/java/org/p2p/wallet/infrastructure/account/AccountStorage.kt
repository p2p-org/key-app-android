package org.p2p.wallet.infrastructure.account

import androidx.core.content.edit
import android.content.Context
import com.google.gson.Gson
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.wallet.infrastructure.account.AccountStorageContract.Key
import kotlin.reflect.KClass

class AccountStorage(
    context: Context,
    private val keyStoreWrapper: KeyStoreWrapper,
    private val gson: Gson
) : AccountStorageContract {

    private val prefsName: String = "${context.packageName}.account_prefs"
    private val sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    override fun <T> saveObject(key: Key, data: T) {
        val objectAsJson = gson.toJson(data)
        val encodedData = keyStoreWrapper.encode(key.prefsValue, objectAsJson)
        sharedPreferences.edit { putString(key.prefsValue, encodedData) }
    }

    @Throws(IllegalArgumentException::class)
    override fun <T : Any> getObject(key: Key, type: KClass<T>): T? {
        return sharedPreferences.getString(key.prefsValue, null)
            ?.let { keyStoreWrapper.decode(key.prefsValue, it) }
            ?.let { gson.fromJson(it, type.java) }
    }

    override fun saveString(key: Key, data: String) {
        val encodedData = keyStoreWrapper.encode(key.prefsValue, data)
        sharedPreferences.edit { putString(key.prefsValue, encodedData) }
    }

    override fun getString(key: Key): String? {
        val encodedData = sharedPreferences.getString(key.prefsValue, null)
        return encodedData?.let { keyStoreWrapper.decode(key.prefsValue, it) }
    }

    override fun contains(key: Key): Boolean = sharedPreferences.contains(key.prefsValue)

    override fun remove(key: Key) {
        sharedPreferences.edit { remove(key) }
        keyStoreWrapper.delete(key.prefsValue)
    }

    override fun removeAll() {
        sharedPreferences.all.forEach { (key, _) ->
            keyStoreWrapper.delete(key)
        }
        sharedPreferences.edit().clear()
    }
}

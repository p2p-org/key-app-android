package org.p2p.wallet.infrastructure.account

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.wallet.infrastructure.account.AccountStorageContract.Key
import timber.log.Timber
import kotlin.reflect.KClass

private const val TAG = "AccountStorage"

class AccountStorage(
    private val keyStoreWrapper: KeyStoreWrapper,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : AccountStorageContract {

    override fun <T> saveObject(key: Key, data: T) {
        tryWithLog(key) {
            val objectAsJson = gson.toJson(data)
            val encodedData = keyStoreWrapper.encode(key.prefsValue, objectAsJson)
            sharedPreferences.edit { putString(key.prefsValue, encodedData) }
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun <T : Any> getObject(key: Key, type: KClass<T>): T? {
        return tryWithLog(key) {
            sharedPreferences.getString(key.prefsValue, null)
                ?.let { keyStoreWrapper.decode(key.prefsValue, it) }
                ?.let { gson.fromJson(it, type.java) }
        }
    }

    override fun saveString(key: Key, data: String) {
        return tryWithLog(key) {
            val encodedData = keyStoreWrapper.encode(key.prefsValue, data)
            sharedPreferences.edit { putString(key.prefsValue, encodedData) }
        }
    }

    override fun getString(key: Key): String? {
        return tryWithLog(key) {
            val encodedData = sharedPreferences.getString(key.prefsValue, null)
            encodedData?.let { keyStoreWrapper.decode(key.prefsValue, it) }
        }
    }

    override fun contains(key: Key): Boolean = sharedPreferences.contains(key.prefsValue)

    override fun remove(key: Key) {
        tryWithLog(key) {
            sharedPreferences.edit { remove(key.prefsValue) }
            keyStoreWrapper.delete(key.prefsValue)
        }
    }

    override fun removeAll() {
        sharedPreferences.all.forEach { (key, _) ->
            keyStoreWrapper.delete(key)
        }
        sharedPreferences.edit { clear() }
    }

    private fun <T> tryWithLog(key: Key, block: () -> T): T = try {
        block()
    } catch (e: Throwable) {
        Timber.tag(TAG).i("Failed working with the ${key.prefsValue}")
        Timber.tag(TAG).i(sharedPreferences.all.keys.toString())
        throw e
    }
}

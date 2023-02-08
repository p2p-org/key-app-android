package org.p2p.wallet.infrastructure.account

import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.wallet.datastore.preferences.UserPreferencesStore
import org.p2p.wallet.infrastructure.account.AccountStorageContract.Key
import timber.log.Timber
import kotlin.reflect.KClass

private const val TAG = "AccountStorage"

class AccountStorage(
    private val keyStoreWrapper: KeyStoreWrapper,
    private val dataStore: UserPreferencesStore,
    private val gson: Gson
) : AccountStorageContract {

    override suspend fun <T> saveObject(key: Key, data: T) {
        tryWithLog(key) {
            val objectAsJson = gson.toJson(data)
            val encodedData = keyStoreWrapper.encode(key.prefsValue, objectAsJson)
            dataStore.putString(key.prefsValue, encodedData)
        }
    }

    suspend fun test(): Int? {
        return flowOf(1, 2, 3).firstOrNull()
    }

    @Throws(IllegalArgumentException::class)
    override suspend fun <T : Any> getObject(key: Key, type: KClass<T>): T? {
        return tryWithLog(key) {
            dataStore.getString(key.prefsValue)
                ?.let { keyStoreWrapper.decode(key.prefsValue, it) }
                ?.let { gson.fromJson(it, type.java) }
        }
    }

    override suspend fun saveString(key: Key, data: String) {
        return tryWithLog(key) {
            val encodedData = keyStoreWrapper.encode(key.prefsValue, data)
            dataStore.putString(key.prefsValue, encodedData)
        }
    }

    override suspend fun getString(key: Key): String? {
        return tryWithLog(key) {
            val encodedData = dataStore.getString(key.prefsValue)
            encodedData?.let { keyStoreWrapper.decode(key.prefsValue, it) }
        }
    }

    override suspend fun contains(key: Key): Boolean = dataStore.contains(key.prefsValue)

    override suspend fun remove(key: Key) {
        tryWithLog(key) {
            dataStore.remove(key.prefsValue)
            keyStoreWrapper.delete(key.prefsValue)
        }
    }

    override suspend fun removeAll() {
        //TODO provide class with keys for auth shares, now it uses only in debug menu
    }

    private suspend fun <T> tryWithLog(key: Key, block: suspend () -> T): T = try {
        block()
    } catch (e: Throwable) {
        Timber.tag(TAG).i("Failed working with the ${key.prefsValue}")
        Timber.tag(TAG).i("Key = ${key.prefsValue} is in dataStore = ${dataStore.contains(key.prefsValue)}")
        throw e
    }
}

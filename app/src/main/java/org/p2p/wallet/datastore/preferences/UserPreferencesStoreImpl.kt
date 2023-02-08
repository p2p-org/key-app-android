package org.p2p.wallet.datastore.preferences

import kotlinx.coroutines.flow.firstOrNull
import org.p2p.wallet.datastore.UserDataStore

class UserPreferencesStoreImpl(
    private val dataStore: UserDataStore
) : UserPreferencesStore {

    override suspend fun getString(key: String): String? {
        return dataStore.getStringFlow(key).firstOrNull()
    }

    override suspend fun getInt(key: String): Int? {
        return dataStore.getIntFlow(key).firstOrNull()
    }

    override suspend fun getBoolean(key: String): Boolean {
        return dataStore.getBooleanFlow(key).firstOrNull() ?: false
    }

    override suspend fun getDouble(key: String): Double? {
        return dataStore.getDoubleFlow(key).firstOrNull()
    }

    override suspend fun contains(key: String): Boolean {
        return dataStore.contains(key).firstOrNull() ?: false
    }

    override suspend fun remove(key: String) {
        dataStore.edit { editor -> editor.remove(key) }
    }

    override suspend fun clear() {
        dataStore.edit { editor -> editor.clear() }
    }

    override suspend fun putString(key: String, value: String) {
        dataStore.edit { editor ->
            editor.putString(key, value)
        }
    }

    override suspend fun putInt(key: String, value: Int) {
        dataStore.edit { editor ->
            editor.putInt(key, value)
        }
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        dataStore.edit { editor ->
            editor.putBoolean(key, value)
        }
    }

    override suspend fun putDouble(key: String, value: Double) {
        dataStore.edit { editor ->
            editor.putDouble(key, value)
        }
    }
}

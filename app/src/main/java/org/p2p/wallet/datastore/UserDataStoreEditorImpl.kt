package org.p2p.wallet.datastore

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf

class UserDataStoreEditorImpl(private val preferences: MutablePreferences) : UserDataStore.Editor {

    override suspend fun removeString(key: String) {
        preferences.remove(stringPreferencesKey(key))
    }

    override suspend fun removeInt(key: String) {
        preferences.remove(intPreferencesKey(key))
    }

    override suspend fun removeDouble(key: String) {
        preferences.remove(doublePreferencesKey(key))
    }

    override suspend fun removeBoolean(key: String) {
        preferences.remove(booleanPreferencesKey(key))
    }

    override suspend fun putString(key: String, value: String) {
        preferences[stringPreferencesKey(key)] = value
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        preferences[booleanPreferencesKey(key)] = value
    }

    override suspend fun putInt(key: String, value: Int) {
        preferences[intPreferencesKey(key)] = value
    }

    override suspend fun putDouble(key: String, value: Double) {
        preferences[doublePreferencesKey(key)] = value
    }

    override suspend fun remove(key: String) {
        preferences.remove(stringPreferencesKey(key))
    }

    override suspend fun clear() {
        preferences.clear()
    }
}

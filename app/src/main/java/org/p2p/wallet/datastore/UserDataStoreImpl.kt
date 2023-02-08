package org.p2p.wallet.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserDataStoreImpl(
    private val dataStore: DataStore<Preferences>
) : UserDataStore {

    override suspend fun edit(block: suspend (UserDataStore.Editor) -> Unit) {
        dataStore.edit {
            block.invoke(UserDataStoreEditorImpl(it))
        }
    }

    override fun getStringFlow(key: String): Flow<String?> {
        return dataStore.data.map { preferences ->
            val value = preferences[stringPreferencesKey(key)]
            value
        }
    }

    override fun getBooleanFlow(key: String): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            val value = preferences[booleanPreferencesKey(key)]
            value ?: false
        }
    }

    override fun getIntFlow(key: String): Flow<Int?> {
        return dataStore.data.map { preferences ->
            val value = preferences[intPreferencesKey(key)]
            value
        }
    }

    override fun getDoubleFlow(key: String): Flow<Double?> {
        return dataStore.data.map { preferences ->
            val value = preferences[doublePreferencesKey(key)]
            value
        }
    }

    override fun contains(key: String): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences.contains(stringPreferencesKey(key))
        }
    }
}

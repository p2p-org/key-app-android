package org.p2p.wallet.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object UserDataStoreService {

    private const val DATA_STORE_NAME = "KEY_APP_DATA_STORE"

    val DATA_STORE_MIGRATION_PREFS_NAMES = listOf(
        "toggle_prefs", "account_prefs", "prefs"
    )

    fun create(
        context: Context,
        preferences: List<SharedPreferences>
    ): DataStore<Preferences> {

        val isPreferencesEmpty = preferences.any { it.all.isEmpty() }

        val migrationPreferencesList = if (!isPreferencesEmpty) {
            DATA_STORE_MIGRATION_PREFS_NAMES
        } else {
            emptyList()
        }
        val dataStore = preferencesDataStore(name = DATA_STORE_NAME, produceMigrations = { context ->
            migrationPreferencesList.map { preferencesName ->
                SharedPreferencesMigration(
                    context = context, sharedPreferencesName = preferencesName
                )
            }
        })
        preferences.forEach { it.edit { clear() } }

        return dataStore
    }
}


package org.p2p.wallet.common

import androidx.core.content.edit
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BooleanPreference(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: Boolean
) : ReadWriteProperty<Any, Boolean> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        preferences.edit {
            putBoolean(key, value)
        }
    }
}

class StringPreference(
    private val preferences: SharedPreferences,
    private val key: String,
    private val defaultValue: String?
) : ReadWriteProperty<Any, String?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return preferences.getString(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        preferences.edit {
            value?.let { putString(key, it) } ?: remove(key)
        }
    }
}

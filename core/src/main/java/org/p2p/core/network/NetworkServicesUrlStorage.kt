package org.p2p.core.network

import androidx.core.content.edit
import android.content.SharedPreferences

class NetworkServicesUrlStorage(
    private val sharedPreferences: SharedPreferences
) {

    fun getString(key: String, defaultValue: String? = null): String? = sharedPreferences.getString(key, defaultValue)
    fun putString(key: String, value: String?) = sharedPreferences.edit { putString(key, value) }
    fun remove(key: String) = sharedPreferences.edit { remove(key) }

    fun edit(
        commit: Boolean = false,
        action: SharedPreferences.Editor.() -> Unit
    ) {
        sharedPreferences.edit(commit, action)
    }
}

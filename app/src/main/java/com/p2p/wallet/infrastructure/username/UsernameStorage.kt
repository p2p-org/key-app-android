package com.p2p.wallet.infrastructure.username

import android.content.SharedPreferences
import androidx.core.content.edit

class UsernameStorage(
    private val sharedPreferences: SharedPreferences
) : UsernameStorageContract {

    override fun saveString(key: String, data: String) {
        sharedPreferences.edit { putString(key, key) }
    }

    override fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun remove(key: String) {
        sharedPreferences.edit { remove(key) }
    }

    override fun contains(key: String) = sharedPreferences.contains(key)

    override fun clear() {
        sharedPreferences.edit { clear() }
    }
}
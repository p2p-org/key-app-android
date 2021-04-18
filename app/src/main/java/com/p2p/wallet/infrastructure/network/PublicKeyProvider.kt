package com.p2p.wallet.infrastructure.network

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.runBlocking

private const val KEY_PUBLIC_KEY = "KEY_PUBLIC_KEY"

class PublicKeyProvider(
    private val sharedPreferences: SharedPreferences
) {

    var publicKey: String = runBlocking { sharedPreferences.getString(KEY_PUBLIC_KEY, "").orEmpty() }
        set(value) {
            field = value
            runBlocking {
                sharedPreferences.edit { putString(KEY_PUBLIC_KEY, value) }
            }
        }

    fun clear() {
        sharedPreferences.edit { remove(KEY_PUBLIC_KEY) }
    }
}
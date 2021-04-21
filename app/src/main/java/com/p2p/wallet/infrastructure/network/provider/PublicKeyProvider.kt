package com.p2p.wallet.infrastructure.network.provider

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.p2p.wallet.R
import kotlinx.coroutines.runBlocking

private const val KEY_PUBLIC_KEY = "KEY_PUBLIC_KEY"

class PublicKeyProvider(
    private val sharedPreferences: SharedPreferences,
    context: Context
) {

    var publicKey: String = runBlocking { sharedPreferences.getString(KEY_PUBLIC_KEY, "").orEmpty() }
        set(value) {
            field = value
            runBlocking {
                sharedPreferences.edit { putString(KEY_PUBLIC_KEY, value) }
            }
        }

    val programPublicKey: String =
        context.getString(R.string.programPublicKey)

    fun clear() {
        sharedPreferences.edit { remove(KEY_PUBLIC_KEY) }
    }
}
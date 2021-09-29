package com.p2p.wallet.infrastructure.network.provider

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.solanaj.utils.crypto.Base58Utils
import com.p2p.wallet.infrastructure.security.SecureStorage
import kotlinx.coroutines.runBlocking

private const val KEY_PUBLIC_KEY = "KEY_PUBLIC_KEY"
private const val KEY_SECRET_KEY = "KEY_SECRET_KEY"

class TokenKeyProvider(
    private val sharedPreferences: SharedPreferences,
    private val secureStorage: SecureStorage
) {

    var publicKey: String =
        runBlocking {
            val result = secureStorage.getString(KEY_PUBLIC_KEY).orEmpty()
            Base58Utils.decodeToString(result)
        }
        set(value) {
            field = value
            runBlocking {
                val result = Base58Utils.encodeFromString(value)
                secureStorage.saveString(KEY_PUBLIC_KEY, result)
            }
        }

    var secretKey: ByteArray =
        runBlocking {
            val result = secureStorage.getString(KEY_SECRET_KEY).orEmpty()
            Base58Utils.decode(result)
        }
        set(value) {
            field = value
            runBlocking {
                val result = Base58Utils.encode(value)
                secureStorage.saveString(KEY_SECRET_KEY, result)
            }
        }

    fun clear() {
        sharedPreferences.edit { remove(KEY_PUBLIC_KEY) }
    }
}
package com.p2p.wallet.infrastructure.security

import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.solanaj.utils.crypto.Hex
import com.p2p.wallet.common.crypto.keystore.DecodeCipher
import com.p2p.wallet.common.crypto.keystore.EncodeCipher
import com.p2p.wallet.common.crypto.keystore.KeyStoreWrapper

class SecureStorage(
    private val keyStoreWrapper: KeyStoreWrapper,
    private val sharedPreferences: SharedPreferences,
    private val isTesting: Boolean = false
) {

    fun saveString(key: String, data: String) {
        if (isTesting) {
            sharedPreferences.edit { putString(key, data) }
        } else {
            val encodedData = keyStoreWrapper.encode(key, data)
            sharedPreferences.edit { putString(key, encodedData) }
        }
    }

    fun saveString(key: String, data: String, cipher: EncodeCipher) {
        if (isTesting) {
            sharedPreferences.edit { putString(key, data) }
        } else {
            val encodedData = keyStoreWrapper.encode(cipher, data)
            sharedPreferences.edit { putString(key, encodedData) }
        }
    }

    fun getString(key: String): String? {
        val encodedData = sharedPreferences.getString(key, null)
        return if (isTesting) {
            encodedData
        } else {
            encodedData?.let { keyStoreWrapper.decode(key, it) }
        }
    }

    fun getString(key: String, cipher: DecodeCipher): String? {
        val encodedData = sharedPreferences.getString(key, null)
        return if (isTesting) {
            encodedData
        } else {
            encodedData?.let { keyStoreWrapper.decode(cipher, it) }
        }
    }

    fun saveBytes(key: String, data: ByteArray) {
        val string = Hex.encode(data)

        if (isTesting) {
            sharedPreferences.edit { putString(key, Hex.encode(data)) }
        } else {
            val encodedData = keyStoreWrapper.encode(key, string)
            sharedPreferences.edit { putString(key, encodedData) }
        }
    }

    fun getBytes(key: String): ByteArray? {
        val encodedData = sharedPreferences.getString(key, null)
        return if (isTesting) {
            encodedData
                ?.let { Hex.decode(it) }
        } else {
            encodedData
                ?.let { keyStoreWrapper.decode(key, it) }
                ?.let { Hex.decode(it) }
        }
    }

    fun remove(key: String) {
        sharedPreferences.edit { remove(key) }

        if (!isTesting) keyStoreWrapper.delete(key)
    }

    fun contains(key: String) = sharedPreferences.contains(key)

    fun clear() {
        if (!isTesting) keyStoreWrapper.clear()
        sharedPreferences.edit { clear() }
    }
}
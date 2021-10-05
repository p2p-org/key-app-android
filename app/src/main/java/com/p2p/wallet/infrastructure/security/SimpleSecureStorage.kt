package com.p2p.wallet.infrastructure.security

import android.content.SharedPreferences
import androidx.core.content.edit
import com.p2p.wallet.common.crypto.keystore.DecodeCipher
import com.p2p.wallet.common.crypto.keystore.EncodeCipher
import com.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.solanaj.utils.crypto.Hex

class SimpleSecureStorage(
    private val sharedPreferences: SharedPreferences
) : SecureStorageContract {

    override fun saveString(key: String, data: String) {
        sharedPreferences.edit { putString(key, data) }
    }

    override fun saveString(key: String, data: String, cipher: EncodeCipher) {
        sharedPreferences.edit { putString(key, data) }
    }

    override fun getString(key: String): String? =
        sharedPreferences.getString(key, null)

    override fun getString(key: String, cipher: DecodeCipher): String? =
        sharedPreferences.getString(key, null)

    override fun saveBytes(key: String, data: ByteArray) {
        sharedPreferences.edit { putString(key, Hex.encode(data)) }
    }

    override fun getBytes(key: String): ByteArray? {
        val encodedData = sharedPreferences.getString(key, null)
        return encodedData?.let { Hex.decode(it) }
    }

    override fun remove(key: String) {
        sharedPreferences.edit { remove(key) }
    }

    override fun contains(key: String) = sharedPreferences.contains(key)

    override fun clear() {
        sharedPreferences.edit { clear() }
    }
}
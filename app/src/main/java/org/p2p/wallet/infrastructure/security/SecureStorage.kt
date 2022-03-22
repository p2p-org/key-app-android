package org.p2p.wallet.infrastructure.security

import android.content.SharedPreferences
import androidx.core.content.edit
import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.solanaj.utils.crypto.Hex

class SecureStorage(
    private val keyStoreWrapper: KeyStoreWrapper,
    private val sharedPreferences: SharedPreferences
) : SecureStorageContract {

    override fun saveString(key: String, data: String) {
        val encodedData = keyStoreWrapper.encode(key, data)
        sharedPreferences.edit { putString(key, encodedData) }
    }

    override fun saveString(key: String, data: String, cipher: EncodeCipher) {
        val encodedData = keyStoreWrapper.encode(cipher, data)
        sharedPreferences.edit { putString(key, encodedData) }
    }

    override fun getString(key: String): String? {
        val encodedData = sharedPreferences.getString(key, null)
        return encodedData?.let { keyStoreWrapper.decode(key, it) }
    }

    override fun getString(key: String, cipher: DecodeCipher): String? {
        val encodedData = sharedPreferences.getString(key, null)
        return encodedData?.let { keyStoreWrapper.decode(cipher, it) }
    }

    override fun saveBytes(key: String, data: ByteArray) {
        val string = Hex.encode(data)
        val encodedData = keyStoreWrapper.encode(key, string)
        sharedPreferences.edit { putString(key, encodedData) }
    }

    override fun getBytes(key: String): ByteArray? {
        val encodedData = sharedPreferences.getString(key, null)
        return encodedData
            ?.let { keyStoreWrapper.decode(key, it) }
            ?.let { Hex.decode(it) }
    }

    override fun remove(key: String) {
        sharedPreferences.edit { remove(key) }
        keyStoreWrapper.delete(key)
    }

    override fun contains(key: String) = sharedPreferences.contains(key)

    override fun clear() {
        keyStoreWrapper.clear()
        sharedPreferences.edit { clear() }
    }
}

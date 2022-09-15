package org.p2p.wallet.infrastructure.security

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.p2p.solanaj.utils.crypto.Hex
import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import kotlin.reflect.KClass

class SecureStorage(
    private val keyStoreWrapper: KeyStoreWrapper,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : SecureStorageContract {

    override fun saveString(key: String, data: String) {
        val encodedData = keyStoreWrapper.encode(key, data)
        sharedPreferences.edit { putString(key, encodedData) }
    }

    override fun saveString(key: String, data: String, cipher: EncodeCipher) {
        val encodedData = keyStoreWrapper.encode(cipher, data)
        sharedPreferences.edit { putString(key, encodedData) }
    }

    override fun <T> saveObject(key: String, data: T) {
        val objectAsJson = gson.toJson(data)
        val encodedData = keyStoreWrapper.encode(key, objectAsJson)
        sharedPreferences.edit { putString(key, encodedData) }
    }

    override fun <T> saveObjectList(key: String, data: List<T>) {
        saveObject(key, data)
    }

    @Throws(IllegalArgumentException::class)
    override fun <T : Any> getObject(key: String, type: KClass<T>): T? {
        return sharedPreferences.getString(key, null)
            ?.let { keyStoreWrapper.decode(key, it) }
            ?.let { gson.fromJson(it, type.java) }
    }

    override fun <T : Any> getObjectList(key: String): List<T> {
        return sharedPreferences.getString(key, null)
            ?.let { keyStoreWrapper.decode(key, it) }
            ?.let { gson.fromJson(it, object : TypeToken<List<T>>() {}.type) }
            ?: emptyList()
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
        sharedPreferences.edit { clear() }
    }
}

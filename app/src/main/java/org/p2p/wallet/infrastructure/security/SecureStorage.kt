package org.p2p.wallet.infrastructure.security

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.p2p.core.crypto.Hex
import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import org.p2p.wallet.infrastructure.security.SecureStorageContract.Key
import timber.log.Timber
import kotlin.reflect.KClass

private const val TAG = "SecureStorage"

class SecureStorage(
    private val keyStoreWrapper: KeyStoreWrapper,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : SecureStorageContract {

    override fun saveString(key: Key, data: String) {
        tryWithLog(key) {
            val encodedData = keyStoreWrapper.encode(key.prefsValue, data)
            sharedPreferences.edit { putString(key.prefsValue, encodedData) }
        }
    }

    override fun saveString(key: Key, data: String, cipher: EncodeCipher) {
        tryWithLog(key) {
            val encodedData = keyStoreWrapper.encode(cipher, data)
            sharedPreferences.edit { putString(key.prefsValue, encodedData) }
        }
    }

    override fun <T> saveObject(key: Key, data: T) {
        tryWithLog(key) {
            val objectAsJson = gson.toJson(data)
            val encodedData = keyStoreWrapper.encode(key.prefsValue, objectAsJson)
            sharedPreferences.edit { putString(key.prefsValue, encodedData) }
        }
    }

    override fun <T> saveObjectList(key: Key, data: List<T>) {
        saveObject(key, data)
    }

    @Throws(IllegalArgumentException::class)
    override fun <T : Any> getObject(key: Key, type: KClass<T>): T? {
        return tryWithLog(key) {
            sharedPreferences.getString(key.prefsValue, null)
                ?.let { keyStoreWrapper.decode(key.prefsValue, it) }
                ?.let { gson.fromJson(it, type.java) }
        }
    }

    override fun <T : Any> getObjectList(key: Key): List<T> {
        return tryWithLog(key) {
            sharedPreferences.getString(key.prefsValue, null)
                ?.let { keyStoreWrapper.decode(key.prefsValue, it) }
                ?.let { gson.fromJson(it, object : TypeToken<List<T>>() {}.type) }
                ?: emptyList()
        }
    }

    override fun getString(key: Key): String? {
        return tryWithLog(key) {
            val encodedData = sharedPreferences.getString(key.prefsValue, null)
            encodedData?.let { keyStoreWrapper.decode(key.prefsValue, it) }
        }
    }

    override fun getString(key: Key, cipher: DecodeCipher): String? {
        return tryWithLog(key) {
            val encodedData = sharedPreferences.getString(key.prefsValue, null)
            encodedData?.let { keyStoreWrapper.decode(cipher, it) }
        }
    }

    override fun saveBytes(key: Key, data: ByteArray) {
        tryWithLog(key) {
            val string = Hex.encode(data)
            val encodedData = keyStoreWrapper.encode(key.prefsValue, string)
            sharedPreferences.edit { putString(key.prefsValue, encodedData) }
        }
    }

    override fun getBytes(key: Key): ByteArray? {
        return tryWithLog(key) {
            val encodedData = sharedPreferences.getString(key.prefsValue, null)
            encodedData
                ?.let { keyStoreWrapper.decode(key.prefsValue, it) }
                ?.let { Hex.decode(it) }
        }
    }

    override fun putBoolean(key: Key, value: Boolean) {
        tryWithLog(key) {
            // no need to encode boolean values
            sharedPreferences.edit { putBoolean(key.prefsValue, value) }
        }
    }

    override fun getBoolean(key: Key, defaultValue: Boolean): Boolean {
        return tryWithLog(key) {
            sharedPreferences.getBoolean(key.prefsValue, defaultValue)
        }
    }

    override fun remove(key: Key) {
        sharedPreferences.edit { remove(key.prefsValue) }
        keyStoreWrapper.delete(key.prefsValue)
    }

    override fun contains(key: Key): Boolean = sharedPreferences.contains(key.prefsValue)

    override fun clear() {
        sharedPreferences.edit { clear() }
    }

    override fun getEncodeCipher(key: Key): EncodeCipher {
        return tryWithLog(key) { keyStoreWrapper.getEncodeCipher(key.prefsValue) }
    }

    override fun getDecodeCipher(key: Key): DecodeCipher {
        return tryWithLog(key) { keyStoreWrapper.getDecodeCipher(key.prefsValue) }
    }

    private fun <T> tryWithLog(key: Key, block: () -> T): T = try {
        block()
    } catch (e: Throwable) {
        Timber.tag(TAG).i("Failed working with the ${key.prefsValue}")
        Timber.tag(TAG).i(sharedPreferences.all.keys.toString())
        throw e
    }
}

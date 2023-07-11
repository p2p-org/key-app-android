package org.p2p.wallet.common

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import kotlin.reflect.KClass
import org.p2p.core.crypto.Hex
import org.p2p.core.wrapper.HexString
import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper

class EncryptedSharedPreferences(
    private val keyStoreWrapper: KeyStoreWrapper,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {
    /**
     * Pass your own loggingTag if needed:
     *
     * `prefs.loggingTag = "Your tag"`
     */
    var loggingTag: String = "EncryptedSharedPreferences"

    fun saveString(key: String, data: String) {
        logOnExceptionAndReThrow(key) {
            val encodedData: HexString = keyStoreWrapper.encode(key, data)
            sharedPreferences.edit { putString(key, encodedData.rawValue) }
        }
    }

    fun saveString(key: String, data: String, cipher: EncodeCipher) {
        logOnExceptionAndReThrow(key) {
            val encodedData: HexString = keyStoreWrapper.encode(cipher, data)
            sharedPreferences.edit { putString(key, encodedData.rawValue) }
        }
    }

    fun getString(key: String): String? {
        return logOnExceptionAndReThrow(key) {
            val encodedData = sharedPreferences.getString(key, null)
            encodedData?.let { keyStoreWrapper.decode(key, HexString(it)) }
        }
    }

    fun getString(key: String, cipher: DecodeCipher): String? {
        return logOnExceptionAndReThrow(key) {
            val encodedData = sharedPreferences.getString(key, null)
            encodedData?.let { keyStoreWrapper.decode(cipher, HexString(it)) }
        }
    }

    fun <T> saveObject(key: String, data: T) {
        logOnExceptionAndReThrow(key) {
            val objectAsJson = gson.toJson(data)
            val encodedData: HexString = keyStoreWrapper.encode(key, objectAsJson)
            sharedPreferences.edit { putString(key, encodedData.rawValue) }
        }
    }

    /**
     * !!! WARNING !!!
     * DOESN'T WORK WITH CLASSES
     */
    @Deprecated(message = "DOESN'T WORK, use only with primitive types like String")
    fun <T> saveObjectList(key: String, data: List<T>) {
        saveObject(key, data)
    }

    @Throws(IllegalArgumentException::class)
    fun <T : Any> getObject(key: String, type: KClass<T>): T? {
        return logOnExceptionAndReThrow(key) {
            sharedPreferences.getString(key, null)
                ?.let { keyStoreWrapper.decode(key, HexString(it)) }
                ?.let { gson.fromJson(it, type.java) }
        }
    }

    /**
     * !!! WARNING !!!
     * DOESN'T WORK WITH CLASSES
     */
    @Deprecated(message = "DOESN'T WORK, use only with primitive types like String")
    fun <T : Any> getObjectList(key: String): List<T> {
        return logOnExceptionAndReThrow(key) {
            sharedPreferences.getString(key, null)
                ?.let { keyStoreWrapper.decode(key, HexString(it)) }
                ?.let { gson.fromJson(it, object : TypeToken<List<T>>() {}.type) }
                ?: emptyList()
        }
    }

    fun saveBytes(key: String, data: ByteArray) {
        logOnExceptionAndReThrow(key) {
            // crazy, double hex encoding
            val string = HexString(Hex.encode(data))
            val encodedData: HexString = keyStoreWrapper.encode(key, string.rawValue)
            sharedPreferences.edit { putString(key, encodedData.rawValue) }
        }
    }

    fun getBytes(key: String): ByteArray? {
        return logOnExceptionAndReThrow(key) {
            // crazy, double hex encoding
            sharedPreferences.getString(key, null)
                ?.let { HexString(keyStoreWrapper.decode(key, HexString(it))) }
                ?.let { Hex.decode(it.rawValue) }
        }
    }

    fun saveBoolean(key: String, value: Boolean) {
        logOnExceptionAndReThrow(key) {
            // no need to encode boolean values
            sharedPreferences.edit { putBoolean(key, value) }
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return logOnExceptionAndReThrow(key) {
            sharedPreferences.getBoolean(key, defaultValue)
        }
    }

    fun saveLong(key: String, value: Long) {
        logOnExceptionAndReThrow(key) {
            // no need to encode boolean values
            sharedPreferences.edit { putLong(key, value) }
        }
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return logOnExceptionAndReThrow(key) {
            sharedPreferences.getLong(key, defaultValue)
        }
    }

    /**
     * Encodes each element inside the set and saves it to the shared preferences.
     * Set is unique by definition, so when elements are encoded - they are unique too.
     */
    fun saveStringSet(key: String, value: Set<String>) {
        logOnExceptionAndReThrow(key) {
            val encryptedSet = value.map { keyStoreWrapper.encode(key, it).rawValue }.toSet()
            sharedPreferences.edit { putStringSet(key, encryptedSet) }
        }
    }

    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Set<String> {
        return logOnExceptionAndReThrow(key) {
            // The consistency of the stored data is not guaranteed if you return original Set,
            // nor is your ability to modify the instance at all.
            (sharedPreferences.getStringSet(key, defaultValue) ?: defaultValue)
                .map { keyStoreWrapper.decode(key, HexString(it)) }
                .toSet()
        }
    }

    fun getEncodeCipher(key: String): EncodeCipher {
        return logOnExceptionAndReThrow(key) {
            keyStoreWrapper.getEncodeCipher(key)
        }
    }

    fun getDecodeCipher(key: String): DecodeCipher {
        return logOnExceptionAndReThrow(key) {
            keyStoreWrapper.getDecodeCipher(key)
        }
    }

    fun contains(key: String): Boolean = sharedPreferences.contains(key)

    fun remove(key: String) {
        logOnExceptionAndReThrow(key) {
            sharedPreferences.edit { remove(key) }
            keyStoreWrapper.deleteKeyAlias(key)
        }
    }

    fun clear() {
        sharedPreferences.all.forEach { (key, _) ->
            keyStoreWrapper.deleteKeyAlias(key)
        }
        sharedPreferences.edit { clear() }
    }

    private fun <T> logOnExceptionAndReThrow(key: String, block: () -> T): T = try {
        block()
    } catch (e: Throwable) {
        Timber.tag(loggingTag).i("Failed working with the $key")
        Timber.tag(loggingTag).i(sharedPreferences.all.keys.toString())
        throw e
    }
}

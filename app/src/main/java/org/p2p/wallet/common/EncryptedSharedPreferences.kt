package org.p2p.wallet.common

import androidx.core.content.edit
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import org.p2p.core.crypto.Hex
import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper

class EncryptedSharedPreferences(
    private val keyStoreWrapper: KeyStoreWrapper,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) {
    var loggingTag: String = "EncryptedSharedPreferences"

    fun saveString(key: String, data: String) {
        tryWithLog(key) {
            val encodedData = keyStoreWrapper.encode(key, data)
            sharedPreferences.edit { putString(key, encodedData) }
        }
    }

    fun saveString(key: String, data: String, cipher: EncodeCipher) {
        tryWithLog(key) {
            val encodedData = keyStoreWrapper.encode(cipher, data)
            sharedPreferences.edit { putString(key, encodedData) }
        }
    }

    fun getString(key: String): String? {
        return tryWithLog(key) {
            val encodedData = sharedPreferences.getString(key, null)
            encodedData?.let { keyStoreWrapper.decode(key, it) }
        }
    }

    fun getString(key: String, cipher: DecodeCipher): String? {
        return tryWithLog(key) {
            val encodedData = sharedPreferences.getString(key, null)
            encodedData?.let { keyStoreWrapper.decode(cipher, it) }
        }
    }

    fun <T> saveObject(key: String, data: T) {
        tryWithLog(key) {
            val objectAsJson = gson.toJson(data)
            val encodedData = keyStoreWrapper.encode(key, objectAsJson)
            sharedPreferences.edit { putString(key, encodedData) }
        }
    }

    fun <T> saveObjectList(key: String, data: List<T>) {
        saveObject(key, data)
    }

    @Throws(IllegalArgumentException::class)
    fun <T : Any> getObject(key: String, type: KClass<T>): T? {
        return tryWithLog(key) {
            sharedPreferences.getString(key, null)
                ?.let { keyStoreWrapper.decode(key, it) }
                ?.let { gson.fromJson(it, type.java) }
        }
    }

    fun <T : Any> getObjectList(key: String): List<T> {
        return tryWithLog(key) {
            sharedPreferences.getString(key, null)
                ?.let { keyStoreWrapper.decode(key, it) }
                ?.let { gson.fromJson(it, object : TypeToken<List<T>>() {}.type) }
                ?: emptyList()
        }
    }

    fun saveBytes(key: String, data: ByteArray) {
        tryWithLog(key) {
            val string = Hex.encode(data)
            val encodedData = keyStoreWrapper.encode(key, string)
            sharedPreferences.edit { putString(key, encodedData) }
        }
    }

    fun getBytes(key: String): ByteArray? {
        return tryWithLog(key) {
            sharedPreferences.getString(key, null)
                ?.let { keyStoreWrapper.decode(key, it) }
                ?.let { Hex.decode(it) }
        }
    }

    fun putBoolean(key: String, value: Boolean) {
        tryWithLog(key) {
            // no need to encode boolean values
            sharedPreferences.edit { putBoolean(key, value) }
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return tryWithLog(key) {
            sharedPreferences.getBoolean(key, defaultValue)
        }
    }

    fun putLong(key: String, value: Long) {
        tryWithLog(key) {
            // no need to encode boolean values
            sharedPreferences.edit { putLong(key, value) }
        }
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return tryWithLog(key) {
            sharedPreferences.getLong(key, defaultValue)
        }
    }

    fun getEncodeCipher(key: String): EncodeCipher {
        return tryWithLog(key) {
            keyStoreWrapper.getEncodeCipher(key)
        }
    }

    fun getDecodeCipher(key: String): DecodeCipher {
        return tryWithLog(key) {
            keyStoreWrapper.getDecodeCipher(key)
        }
    }

    fun contains(key: String): Boolean = sharedPreferences.contains(key)

    fun remove(key: String) {
        tryWithLog(key) {
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

    private fun <T> tryWithLog(key: String, block: () -> T): T = try {
        block()
    } catch (e: Throwable) {
        Timber.tag(loggingTag).i("Failed working with the $key")
        Timber.tag(loggingTag).i(sharedPreferences.all.keys.toString())
        throw e
    }
}

class StringEncryptedPreference(
    private val preferences: EncryptedSharedPreferences,
    private val key: String,
    private val defaultValue: String? = null
) : ReadWriteProperty<Any, String?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return preferences.getString(key) ?: defaultValue
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        value?.let { preferences.saveString(key, it) } ?: preferences.remove(key)
    }
}

class ObjectEncryptedPreference<Value : Any>(
    private val preferences: EncryptedSharedPreferences,
    private val key: String,
    private val type: KClass<Value>,
    private val defaultValue: Value? = null,
    private val nullIfMappingFailed: Boolean = false
) : ReadWriteProperty<Any, Value?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Value? {
        return kotlin.runCatching { preferences.getObject(key, type) ?: defaultValue }
            .let { if (nullIfMappingFailed) it.getOrNull() else it.getOrThrow() }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Value?) {
        value?.let { preferences.saveObject(key, it) } ?: preferences.remove(key)
    }
}

class LongPreference(
    private val preferences: EncryptedSharedPreferences,
    private val key: String,
    private val defaultValue: Long
) : ReadWriteProperty<Any, Long> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return preferences.getLong(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        preferences.putLong(key, value)
    }
}

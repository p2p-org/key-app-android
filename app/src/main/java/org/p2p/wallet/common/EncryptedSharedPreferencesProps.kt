package org.p2p.wallet.common

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

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
    private val keyProvider: () -> String,
    private val type: KClass<Value>,
    private val defaultValue: Value? = null,
    private val nullIfMappingFailed: Boolean = false
) : ReadWriteProperty<Any, Value?> {
    private val key: String
        get() = keyProvider.invoke()

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
    private val keyProvider: () -> String,
    private val defaultValue: Long,
) : ReadWriteProperty<Any, Long> {
    private val key: String
        get() = keyProvider.invoke()

    override fun getValue(thisRef: Any, property: KProperty<*>): Long {
        return preferences.getLong(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Long) {
        preferences.saveLong(key, value)
    }
}

class BooleanEncryptedPreference(
    private val preferences: EncryptedSharedPreferences,
    private val keyProvider: () -> String,
    private val defaultValue: Boolean
) : ReadWriteProperty<Any, Boolean> {
    private val key: String
        get() = keyProvider.invoke()

    override fun getValue(thisRef: Any, property: KProperty<*>): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        preferences.saveBoolean(key, value)
    }
}

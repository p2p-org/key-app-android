package org.p2p.wallet.infrastructure.account

import kotlin.reflect.KClass

interface AccountStorageContract {
    enum class Key(val prefsValue: String) {
        KEY_LAST_DEVICE_SHARE_ID("KEY_LAST_DEVICE_SHARE_ID"),
        KEY_IN_SIGN_UP_PROCESS("KEY_IN_SIGN_UP_PROCESS"),
    }

    fun <T> saveObject(key: Key, data: T)
    fun <T : Any> getObject(key: Key, type: KClass<T>): T?

    fun saveString(key: Key, data: String)
    fun getString(key: Key): String?

    fun contains(key: Key): Boolean

    fun remove(key: Key)
    fun removeAll()
}

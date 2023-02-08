package org.p2p.wallet.infrastructure.account

import kotlin.reflect.KClass

interface AccountStorageContract {
    enum class Key(
        private val prefsKey: String,
        private var customKey: String? = null
    ) {
        KEY_LAST_DEVICE_SHARE_ID("KEY_LAST_DEVICE_SHARE_ID"),
        KEY_IN_SIGN_UP_PROCESS("KEY_IN_SIGN_UP_PROCESS"),
        KEY_SOLEND_ONBOARDING_COMPLETED("KEY_SHOW_SOLEND_ONBOARDING");

        val prefsValue
            get() = customValue?.let {
                "${prefsKey}_$customValue"
            } ?: prefsKey

        val customValue
            get() = customKey

        companion object {
            fun Key.withCustomKey(custom: String): Key {
                customKey = custom
                return this
            }
        }
    }

    suspend fun <T> saveObject(key: Key, data: T)
    suspend fun <T : Any> getObject(key: Key, type: KClass<T>): T?

    suspend fun saveString(key: Key, data: String)
    suspend fun getString(key: Key): String?

    suspend fun contains(key: Key): Boolean

    suspend fun remove(key: Key)
    suspend fun removeAll()
}

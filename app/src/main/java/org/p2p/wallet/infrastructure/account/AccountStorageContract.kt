package org.p2p.wallet.infrastructure.account

import kotlin.reflect.KClass

interface AccountStorageContract {
    enum class Key(
        private val prefsKey: String,
        private var customKey: String? = null
    ) {
        KEY_LAST_DEVICE_SHARE_ID("KEY_LAST_DEVICE_SHARE_ID"),
        KEY_IN_SIGN_UP_PROCESS("KEY_IN_SIGN_UP_PROCESS"),
        KEY_SOLEND_ONBOARDING_COMPLETED("KEY_SHOW_SOLEND_ONBOARDING"),
        KEY_ONBOARDING_METADATA("KEY_ONBOARDING_METADATA");

        val prefsValue: String
            get() = customValue?.let { "${prefsKey}_$customValue" } ?: prefsKey

        private val customValue: String?
            get() = customKey

        companion object {
            fun Key.withCustomKey(custom: String): Key {
                customKey = custom
                return this
            }
        }
    }

    fun <T> saveObject(key: Key, data: T)
    fun <T : Any> getObject(key: Key, type: KClass<T>): T?

    fun saveString(key: Key, data: String)
    fun getString(key: Key): String?

    fun contains(key: Key): Boolean

    fun remove(key: Key)
    fun removeAll()
}

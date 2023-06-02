package org.p2p.wallet.infrastructure.security

import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import kotlin.reflect.KClass

interface SecureStorageContract {

    enum class Key(
        private val prefsKey: String,
        private var customKey: String? = null
    ) {
        KEY_PIN_CODE_HASH("KEY_PIN_CODE_HASH"),
        KEY_PIN_CODE_BIOMETRIC_HASH("KEY_PIN_CODE_BIOMETRIC_HASH"),
        KEY_PUBLIC_KEY("KEY_PUBLIC_KEY"),
        KEY_SECRET_KEY("KEY_SECRET_KEY"),
        KEY_PIN_CODE_SALT("KEY_PIN_CODE_SALT"),
        KEY_STUB_PUBLIC_KEY("KEY_STUB_PUBLIC_KEY"),
        KEY_USE_STUB_PUBLIC_KEY("KEY_USE_STUB_PUBLIC_KEY"),
        KEY_ONBOARDING_METADATA("KEY_ONBOARDING_METADATA"),
        KEY_SEED_PHRASE("KEY_SEED_PHRASE"),
        KEY_SEED_PHRASE_PROVIDER("KEY_SEED_PHRASE_PROVIDER"),
        KEY_IS_SELL_WARNING_SHOWED("KEY_IS_SELL_WARNING_SHOWED");

        val prefsValue
            get() = customValue?.let {
                "${prefsKey}_$customValue"
            } ?: prefsKey

        private val customValue
            get() = customKey

        companion object {
            fun Key.withCustomKey(custom: String): Key {
                customKey = custom
                return this
            }
        }
    }

    fun saveString(key: Key, data: String)
    fun saveString(key: Key, data: String, cipher: EncodeCipher)

    fun <T> saveObject(key: Key, data: T)
    fun <T> saveObjectList(key: Key, data: List<T>)

    fun getString(key: Key): String?
    fun getString(key: Key, cipher: DecodeCipher): String?

    fun putBoolean(key: Key, value: Boolean)
    fun getBoolean(key: Key, defaultValue: Boolean): Boolean

    fun <T : Any> getObject(key: Key, type: KClass<T>): T?
    fun <T : Any> getObjectList(key: Key): List<T>

    fun saveBytes(key: Key, data: ByteArray)
    fun getBytes(key: Key): ByteArray?

    fun remove(key: Key)

    fun contains(key: Key): Boolean

    fun clear()
    fun getEncodeCipher(key: Key): EncodeCipher
    fun getDecodeCipher(key: Key): DecodeCipher
}

package org.p2p.wallet.infrastructure.security

import kotlin.reflect.KClass
import org.p2p.wallet.common.EncryptedSharedPreferences
import org.p2p.wallet.common.crypto.keystore.DecodeCipher
import org.p2p.wallet.common.crypto.keystore.EncodeCipher
import org.p2p.wallet.infrastructure.security.SecureStorageContract.Key

private const val TAG = "SecureStorage"

/**
 * Cleared on user logout
 */
class SecureStorage(
    private val encryptedPrefs: EncryptedSharedPreferences
) : SecureStorageContract {

    override fun saveString(key: Key, data: String) {
        encryptedPrefs.saveString(key.prefsValue, data)
    }

    override fun saveString(key: Key, data: String, cipher: EncodeCipher) {
        encryptedPrefs.saveString(key.prefsValue, data, cipher)
    }

    override fun <T> saveObject(key: Key, data: T) {
        encryptedPrefs.saveObject(key.prefsValue, data)
    }

    override fun <T> saveObjectList(key: Key, data: List<T>) {
        encryptedPrefs.saveObjectList(key.prefsValue, data)
    }

    @Throws(IllegalArgumentException::class)
    override fun <T : Any> getObject(key: Key, type: KClass<T>): T? {
        return encryptedPrefs.getObject(key.prefsValue, type)
    }

    override fun <T : Any> getObjectList(key: Key): List<T> {
        return encryptedPrefs.getObjectList(key.prefsValue)
    }

    override fun getString(key: Key): String? {
        return encryptedPrefs.getString(key.prefsValue)
    }

    override fun getString(key: Key, cipher: DecodeCipher): String? {
        return encryptedPrefs.getString(key.prefsValue, cipher)
    }

    override fun saveBytes(key: Key, data: ByteArray) {
        encryptedPrefs.saveBytes(key.prefsValue, data)
    }

    override fun getBytes(key: Key): ByteArray? {
        return encryptedPrefs.getBytes(key.prefsValue)
    }

    override fun putBoolean(key: Key, value: Boolean) {
        encryptedPrefs.saveBoolean(key.prefsValue, value)
    }

    override fun getBoolean(key: Key, defaultValue: Boolean): Boolean {
        return encryptedPrefs.getBoolean(key.prefsValue, defaultValue)
    }

    override fun remove(key: Key) {
        encryptedPrefs.remove(key.prefsValue)
    }

    override fun contains(key: Key): Boolean = encryptedPrefs.contains(key.prefsValue)

    override fun clear() {
        encryptedPrefs.clear()
    }

    override fun getEncodeCipher(key: Key): EncodeCipher {
        return encryptedPrefs.getEncodeCipher(key.prefsValue)
    }

    override fun getDecodeCipher(key: Key): DecodeCipher {
        return encryptedPrefs.getDecodeCipher(key.prefsValue)
    }
}

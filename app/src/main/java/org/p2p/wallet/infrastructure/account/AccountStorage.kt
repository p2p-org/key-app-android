package org.p2p.wallet.infrastructure.account

import kotlin.reflect.KClass
import org.p2p.wallet.common.EncryptedSharedPreferences
import org.p2p.wallet.infrastructure.account.AccountStorageContract.Key

/**
 * NOT cleared on user logout
 */
class AccountStorage(
    private val encryptedPrefs: EncryptedSharedPreferences
) : AccountStorageContract {

    init {
        encryptedPrefs.loggingTag = "AccountStorage"
    }

    override fun <T> saveObject(key: Key, data: T) {
        encryptedPrefs.saveObject(key.prefsValue, data)
    }

    @Throws(IllegalArgumentException::class)
    override fun <T : Any> getObject(key: Key, type: KClass<T>): T? {
        return encryptedPrefs.getObject(key.prefsValue, type)
    }

    override fun saveString(key: Key, data: String) {
        encryptedPrefs.saveString(key.prefsValue, data)
    }

    override fun getString(key: Key): String? {
        return encryptedPrefs.getString(key.prefsValue)
    }

    override fun contains(key: Key): Boolean {
        return encryptedPrefs.contains(key.prefsValue)
    }

    override fun remove(key: Key) {
        encryptedPrefs.remove(key.prefsValue)
    }

    override fun removeAll() {
        encryptedPrefs.clear()
    }
}

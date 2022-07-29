package org.p2p.wallet.auth.common

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.p2p.wallet.auth.model.DeviceShareKey
import org.p2p.wallet.infrastructure.security.SecureStorageContract
import java.lang.reflect.Type

private const val KEY_DEVICE_SHARES = "KEY_DEVICE_SHARES"
private const val KEY_LAST_DEVICE_SHARE_ID = "KEY_LAST_DEVICE_SHARE_ID"

class DeviceShareStorage(
    private val gson: Gson,
    private val secureStorage: SecureStorageContract,
    private val sharedPreferences: SharedPreferences,
) {

    fun saveDeviceShare(deviceShare: String, userId: String): Boolean {
        return parseDeviceShare(deviceShare)?.let { share ->
            share.userId = userId
            saveDeviceShare(userId, share)
            secureStorage.saveString(KEY_LAST_DEVICE_SHARE_ID, userId)
            true
        } ?: false
    }

    private fun parseDeviceShare(deviceShare: String): DeviceShareKey? {
        return gson.fromJson(deviceShare, DeviceShareKey::class.java)
    }

    fun hasDeviceShare(): Boolean = sharedPreferences.contains(KEY_DEVICE_SHARES)

    fun getDeviceShare(userId: String): DeviceShareKey? = getDeviceSharesMap()[userId]

    fun getLastDeviceShareUserId(): String? = secureStorage.getString(KEY_LAST_DEVICE_SHARE_ID)

    private fun saveDeviceShare(userId: String, share: DeviceShareKey) {
        val sharesMap = getDeviceSharesMap()
        sharesMap[userId] = share
        secureStorage.saveString(KEY_DEVICE_SHARES, gson.toJson(sharesMap))
    }

    private fun getDeviceSharesMap(): MutableMap<String, DeviceShareKey> {
        return secureStorage.getString(KEY_DEVICE_SHARES)?.let { sharesMap ->
            val type: Type = object : TypeToken<HashMap<String, DeviceShareKey>>() {}.type
            gson.fromJson(sharesMap, type)
        } ?: mutableMapOf()
    }
}

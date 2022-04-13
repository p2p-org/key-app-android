package org.p2p.wallet.push_notifications.ineractor

import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.push_notifications.model.DeviceInfo
import org.p2p.wallet.push_notifications.model.DeviceToken
import org.p2p.wallet.push_notifications.repository.NotificationServiceRepository
import org.p2p.wallet.push_notifications.repository.PushTokenRepository

private const val KEY_DEVICE_TOKEN = "KEY_DEVICE_TOKEN"
private const val DEVICE_TOKEN = "device_token"
private const val DEVICE_INFO = "device_info"
private const val CLIENT_ID = "client_id"

class NotificationServiceInteractor(
    private val notificationServiceRepository: NotificationServiceRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences,
) {

    suspend fun updateDeviceToken(deviceToken: String): DeviceToken {
        sharedPreferences.edit { putString(KEY_DEVICE_TOKEN, deviceToken) }

        val deviceInfo = DeviceInfo(
            osName = "Android",
            osVersion = Build.VERSION.RELEASE,
            deviceModel = Build.MANUFACTURER + ' ' + Build.MODEL
        )

        val params = mapOf(
            DEVICE_TOKEN to deviceToken,
            DEVICE_INFO to deviceInfo,
            CLIENT_ID to tokenKeyProvider.publicKey
        )

        return notificationServiceRepository.sendDeviceToken(params)
    }

    suspend fun deleteDeviceToken() {
        val deviceToken = sharedPreferences.getString(KEY_DEVICE_TOKEN, null) ?: return
        sharedPreferences.edit { remove(KEY_DEVICE_TOKEN) }

        val params = mapOf(
            DEVICE_TOKEN to deviceToken,
            CLIENT_ID to tokenKeyProvider.publicKey
        )

        pushTokenRepository.deletePushToken()
        notificationServiceRepository.deleteDeviceToken(params)
    }
}

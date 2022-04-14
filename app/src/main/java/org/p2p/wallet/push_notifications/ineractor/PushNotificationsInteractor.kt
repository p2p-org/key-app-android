package org.p2p.wallet.push_notifications.ineractor

import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.push_notifications.model.DeviceInfo
import org.p2p.wallet.push_notifications.model.DeviceToken
import org.p2p.wallet.push_notifications.repository.DeviceTokenRepository
import org.p2p.wallet.push_notifications.repository.PushTokenRepository

private const val KEY_DEVICE_TOKEN = "KEY_DEVICE_TOKEN"

class PushNotificationsInteractor(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences,
) {

    suspend fun updateDeviceToken(): DeviceToken {
        val token = pushTokenRepository.getPushToken().value

        sharedPreferences.edit { putString(KEY_DEVICE_TOKEN, token) }

        val deviceInfo = DeviceInfo(
            osName = "Android",
            osVersion = Build.VERSION.RELEASE,
            deviceModel = Build.MANUFACTURER + ' ' + Build.MODEL
        )

        val deviceToken = DeviceToken(
            deviceToken = token,
            deviceInfo = deviceInfo,
            clientId = tokenKeyProvider.publicKey
        )

        return deviceTokenRepository.sendDeviceToken(deviceToken)
    }

    suspend fun deleteDeviceToken() {
        val token = sharedPreferences.getString(KEY_DEVICE_TOKEN, null) ?: return
        sharedPreferences.edit { remove(KEY_DEVICE_TOKEN) }

        val deviceToken = DeviceToken(
            deviceToken = token,
            clientId = tokenKeyProvider.publicKey
        )

        deviceTokenRepository.deleteDeviceToken(deviceToken)
    }
}

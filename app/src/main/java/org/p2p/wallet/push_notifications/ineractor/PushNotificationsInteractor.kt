package org.p2p.wallet.push_notifications.ineractor

import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.push_notifications.model.DeviceInfo
import org.p2p.wallet.push_notifications.model.DeviceToken
import org.p2p.wallet.push_notifications.repository.DeviceTokenRepository
import org.p2p.wallet.push_notifications.repository.PushTokenRepository

private const val KEY_DEVICE_TOKEN = "KEY_DEVICE_TOKEN"
private const val TOKEN_SEND_RETRY_DELAY_MS = 60000L
private const val RETRIES_NUMBER = 1

class PushNotificationsInteractor(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences,
    private val appScope: AppScope
) {

    suspend fun updateDeviceToken(retries: Int = RETRIES_NUMBER) {
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

        try {
            deviceTokenRepository.sendDeviceToken(deviceToken)
        } catch (e: ServerException) {
            retryTokenSend(retries)
        }
    }

    private fun retryTokenSend(retries: Int) {
        if (retries < 1) return

        appScope.launch {
            delay(TOKEN_SEND_RETRY_DELAY_MS)
            updateDeviceToken(retries - 1)
        }
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

package org.p2p.wallet.push_notifications.ineractor

import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import kotlinx.coroutines.delay
import org.p2p.wallet.infrastructure.network.data.ServerException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.push_notifications.model.DeviceInfo
import org.p2p.wallet.push_notifications.model.DeviceToken
import org.p2p.wallet.push_notifications.repository.DeviceTokenRepository
import org.p2p.wallet.push_notifications.repository.PushTokenRepository
import timber.log.Timber

private const val KEY_DEVICE_TOKEN = "KEY_DEVICE_TOKEN"
private const val TOKEN_SEND_RETRY_DELAY_MS = 60000L
private const val DEFAULT_RETRIES_NUMBER = 1
private const val TAG_NOTIFICATION_SERVICE = "NotificationService"

class PushNotificationsInteractor(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val sharedPreferences: SharedPreferences
) {

    suspend fun updateDeviceToken(retries: Int = DEFAULT_RETRIES_NUMBER) {
        if (retries < 0) return

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
            Timber.tag(TAG_NOTIFICATION_SERVICE).e(e, "Error sending device token $token to server")
            delay(TOKEN_SEND_RETRY_DELAY_MS)
            updateDeviceToken(retries - 1)
        } catch (e: Throwable) {
            // todo: Delete this catch after using real NotificationService
            // https://p2pvalidator.atlassian.net/browse/PWN-3355
        }
    }

    suspend fun deleteDeviceToken() {
        sharedPreferences.edit { remove(KEY_DEVICE_TOKEN) }

        val deviceToken = DeviceToken(
            deviceToken = pushTokenRepository.getPushToken().value,
            clientId = tokenKeyProvider.publicKey
        )

        try {
            deviceTokenRepository.deleteDeviceToken(deviceToken)
        } catch (e: Throwable) {
            Timber.tag(TAG_NOTIFICATION_SERVICE).e(e, "Error deleting device token on server")
        }
    }
}

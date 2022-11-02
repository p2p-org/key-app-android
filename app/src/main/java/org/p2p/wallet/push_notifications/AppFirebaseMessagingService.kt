package org.p2p.wallet.push_notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.wallet.appsflyer.AppsFlyerService
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.notification.FcmPushNotificationData
import org.p2p.wallet.notification.NotificationType
import timber.log.Timber

private const val TAG = "AppFirebaseMessagingService"

class AppFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val appNotificationManager: AppNotificationManager by inject()
    private val appsFlyerService: AppsFlyerService by inject()

    override fun onNewToken(token: String) {
        appsFlyerService.onNewToken(token)
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (appsFlyerService.isUninstallTrackingMessage(message)) {
            return
        }
        handleForegroundPush(message)
    }

    private fun handleForegroundPush(message: RemoteMessage) {
        Timber.tag(TAG).d("From: ${message.from}")

        val notificationType = message.data[AppDeeplinksManager.NOTIFICATION_TYPE]?.let {
            NotificationType.fromValue(it)
        } ?: NotificationType.DEFAULT

        message.notification?.let {
            appNotificationManager.showFcmPushNotification(
                FcmPushNotificationData(
                    title = it.title.orEmpty(),
                    body = it.body.orEmpty(),
                    type = notificationType
                )
            )
        }
    }
}

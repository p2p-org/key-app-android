package org.p2p.wallet.push_notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.wallet.common.di.ServiceScope
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.notification.FcmPushNotificationData
import org.p2p.wallet.notification.NotificationType
import org.p2p.wallet.utils.NoOp
import timber.log.Timber

private const val TAG = "AppFirebaseMessagingService"
private const val NOTIFICATION_TYPE = "eventType"

class AppFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val serviceScope: ServiceScope by inject()

    private val appNotificationManager: AppNotificationManager by inject()

    override fun onNewToken(token: String) {
        NoOp
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        handleForegroundPush(message)
    }

    private fun handleForegroundPush(message: RemoteMessage) {
        Timber.tag(TAG).d("From: ${message.from}")

        var notificationType = NotificationType.DEFAULT

        if (message.data.isNotEmpty()) {
            notificationType = NotificationType.fromValue(
                message.data[NOTIFICATION_TYPE].orEmpty()
            )
        }

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

package org.p2p.wallet.push_notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.wallet.appsflyer.AppsFlyerService
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import org.p2p.wallet.intercom.IntercomPushService
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.notification.FcmPushNotificationData
import org.p2p.wallet.notification.NotificationType
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.push_notifications.ineractor.PushNotificationsInteractor

private const val TAG = "AppFirebaseMessagingService"

class AppFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val appNotificationManager: AppNotificationManager by inject()
    private val appsFlyerService: AppsFlyerService by inject()
    private val intercomPushService: IntercomPushService by inject()
    private val pushNotificationsInteractor: PushNotificationsInteractor by inject()
    private val appScope: AppScope by inject()

    override fun onNewToken(token: String) {
        appsFlyerService.onNewToken(token)
        intercomPushService.registerForPush(token)
        appScope.launch { pushNotificationsInteractor.updateDeviceToken() }
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        when {
            appsFlyerService.isUninstallTrackingMessage(message) -> {
                return
            }
            intercomPushService.isIntercomPush(message) -> {
                intercomPushService.handlePush(message)
            }
            else -> {
                handleForegroundPush(message)
            }
        }
    }

    private fun handleForegroundPush(message: RemoteMessage) {
        Timber.tag(TAG).d("From: ${message.from}")

        val notificationType = message.data[AppDeeplinksManager.NOTIFICATION_TYPE]
            ?.let { NotificationType.fromValue(it) }
            ?: NotificationType.DEFAULT

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

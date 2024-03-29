package org.p2p.wallet.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import org.p2p.wallet.R
import org.p2p.wallet.deeplinks.AppDeeplinksManager
import java.util.UUID

private const val KEY_APP_NOTIFICATION_CHANNEL_ID = "KEY_APP_WALLET_CHANNEL_ID"
private const val KEY_APP_NOTIFICATION_CHANNEL_NAME = "Key App"

private const val NOTIFICATION_MANAGER_REQUEST_CODE = 1

class AppNotificationManager(
    private val context: Context,
    private val deeplinksManager: AppDeeplinksManager
) {
    companion object {
        fun createNotificationChannels(context: Context) {
            val channels = getNotificationChannels()
            val notificationManager = NotificationManagerCompat.from(context)
            channels.forEach { channel -> notificationManager.createNotificationChannel(channel) }
        }

        private fun getNotificationChannels(): Set<NotificationChannel> = setOf(
            NotificationChannel(
                KEY_APP_NOTIFICATION_CHANNEL_ID,
                KEY_APP_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private val notificationManager = context.getSystemService<NotificationManager>()!!

    fun showSwapTransactionNotification(data: SwapTransactionNotification) {
        val contentIntent = createPendingContentIntent()

        val notification = createDefaultNotificationBuilder(contentIntent)
            .setContentTitle(context.getString(R.string.main_send_success))
            .setContentText(data.buildShortText(context))
            .setVibrate(data.vibration)
            .setSound(data.sound)
            .build()

        notificationManager.notify(data.notificationId, notification)
    }

    fun showErrorTransactionNotification(data: ErrorTransactionNotification) {
        val contentIntent = createPendingContentIntent()

        val notification = createDefaultNotificationBuilder(contentIntent)
            .setContentTitle(data.buildTitleText(context))
            .setContentText(data.message)
            .setVibrate(data.vibration)
            .setSound(data.sound)
            .build()

        notificationManager.notify(data.notificationId, notification)
    }

    fun showFcmPushNotification(data: FcmPushNotificationData) {
        val contentIntent = createPendingContentIntent(data.type)

        val notification = createDefaultNotificationBuilder(contentIntent)
            .setContentTitle(data.title)
            .setContentText(data.body)
            .build()

        val notificationId = UUID.randomUUID().toString().hashCode()
        notificationManager.notify(notificationId, notification)
    }

    private fun createDefaultNotificationBuilder(contentIntent: PendingIntent?): NotificationCompat.Builder =
        NotificationCompat.Builder(context, KEY_APP_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_push_notification)
            .setColor(context.getColor(R.color.lime))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    private fun createPendingContentIntent(type: NotificationType = NotificationType.DEFAULT): PendingIntent? {
        val pushIntent = deeplinksManager.buildIntent(type)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(
            context,
            NOTIFICATION_MANAGER_REQUEST_CODE,
            pushIntent,
            flags
        )
    }
}

package org.p2p.wallet.notification

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import org.p2p.wallet.R
import org.p2p.wallet.root.RootActivity
import java.util.UUID

private const val P2P_WALLET_CHANNEL_ID = "P2P_WALLET_CHANNEL_ID"

class AppNotificationManager(private val context: Context) {
    companion object {
        fun createNotificationChannels(context: Context) {
            val channels = getNotificationChannels()
            val notificationManager = NotificationManagerCompat.from(context)
            channels.forEach { channel -> notificationManager.createNotificationChannel(channel) }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun getNotificationChannels(): Set<NotificationChannel> = setOf(
            NotificationChannel(
                P2P_WALLET_CHANNEL_ID,
                "P2P Swap",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showSwapTransactionNotification(data: SwapTransactionNotification) {
        val intent = buildPushIntent(context)
        val contentIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = createDefaultNotificationBuilder(contentIntent)
            .setContentTitle(context.getString(R.string.main_send_success))
            .setContentText(data.buildShortText(context))
            .setVibrate(data.vibration)
            .setSound(data.sound)
            .build()

        notificationManager.notify(data.notificationId, notification)
    }

    fun showErrorTransactionNotification(data: ErrorTransactionNotification) {
        val intent = buildPushIntent(context)
        val contentIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = createDefaultNotificationBuilder(contentIntent)
            .setContentTitle(data.buildTitleText(context))
            .setContentText(data.message)
            .setVibrate(data.vibration)
            .setSound(data.sound)
            .build()

        notificationManager.notify(data.notificationId, notification)
    }

    fun showFcmPushNotification(data: FcmPushNotificationData) {
        val intent = buildPushIntent(context)
        val contentIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = createDefaultNotificationBuilder(contentIntent)
            .setContentTitle(data.title)
            .setContentText(data.body)
            .build()

        notificationManager.notify(
            // todo: replace with correct id in PWN-3113
            UUID.randomUUID().toString().hashCode(),
            notification
        )
    }

    private fun createDefaultNotificationBuilder(contentIntent: PendingIntent?): NotificationCompat.Builder =
        NotificationCompat.Builder(context, P2P_WALLET_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

private fun buildPushIntent(context: Context): Intent {
    val activityManager = ContextCompat.getSystemService(context, ActivityManager::class.java)
    return activityManager?.appTasks?.firstOrNull()?.taskInfo?.baseIntent ?: RootActivity.createIntent(context)
}
}

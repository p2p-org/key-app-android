package org.p2p.wallet.debug.pushnotifications

import androidx.annotation.StringRes
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.notification.FcmPushNotificationData
import org.p2p.wallet.notification.NotificationType
import org.p2p.wallet.settings.model.SettingsRow

class PushNotificationsPresenter(
    private val appNotificationManager: AppNotificationManager
) : BasePresenter<PushNotificationsContract.View>(), PushNotificationsContract.Presenter {

    override fun loadNotifications() {
        view?.showNotifications(getNotifications())
    }

    override fun onNotificationClicked(@StringRes titleResId: Int) {
        val notificationTitle = "Test PushNotification"
        when (titleResId) {
            R.string.debug_notifications_item_default -> appNotificationManager.showFcmPushNotification(
                FcmPushNotificationData(
                    notificationTitle,
                    "Navigate to Home",
                    NotificationType.DEFAULT
                )
            )
            R.string.debug_notifications_item_receive -> appNotificationManager.showFcmPushNotification(
                FcmPushNotificationData(
                    notificationTitle,
                    "Navigate to History",
                    NotificationType.RECEIVE
                )
            )
        }
    }

    private fun getNotifications(): List<SettingsRow.Section> {
        return listOf(
            SettingsRow.Section(
                titleResId = R.string.debug_notifications_item_default,
                iconRes = R.drawable.ic_settings_notification,
                subtitle = "With navigation to Home"
            ),
            SettingsRow.Section(
                titleResId = R.string.debug_notifications_item_receive,
                iconRes = R.drawable.ic_settings_notification,
                subtitle = "With navigation to History"
            ),
        )
    }
}

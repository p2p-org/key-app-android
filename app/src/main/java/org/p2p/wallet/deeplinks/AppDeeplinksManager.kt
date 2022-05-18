package org.p2p.wallet.deeplinks

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import org.p2p.wallet.R
import org.p2p.wallet.notification.NotificationType
import org.p2p.wallet.root.RootActivity

class AppDeeplinksManager(private val context: Context) {

    companion object {
        const val DEEPLINK_MAIN_SCREEN_EXTRA = "DEEPLINK_SCREEN_EXTRA"
    }

    var mainTabsSwitcher: MainTabsSwitcher? = null

    private var pendingIntent: Intent? = null

    fun buildIntent(notificationType: NotificationType): Intent {
        val activityManager = context.getSystemService<ActivityManager>()
        val intent = activityManager?.appTasks
            ?.firstOrNull()
            ?.taskInfo
            ?.baseIntent
            ?: RootActivity.createIntent(context)
        return intent.apply {
            addDeeplinkDataToIntent(notificationType)
        }
    }

    private fun Intent.addDeeplinkDataToIntent(notificationType: NotificationType) {
        if (notificationType == NotificationType.RECEIVE) {
            putExtra(DEEPLINK_MAIN_SCREEN_EXTRA, R.id.itemHistory)
        }
    }

    fun handleOrSaveDeeplinkIntent(intent: Intent) {
        val extras = intent.extras ?: return
        if (extras.containsKey(DEEPLINK_MAIN_SCREEN_EXTRA)) {
            mainTabsSwitcher?.navigate(extras.getInt(DEEPLINK_MAIN_SCREEN_EXTRA)) ?: savePendingIntent(intent)
        }
    }

    fun handleSavedDeeplinkIntent() {
        pendingIntent?.let {
            handleOrSaveDeeplinkIntent(it)
            pendingIntent = null
        }
    }

    private fun savePendingIntent(intent: Intent) {
        pendingIntent = intent
    }
}

interface MainTabsSwitcher {
    fun navigate(itemId: Int)
}

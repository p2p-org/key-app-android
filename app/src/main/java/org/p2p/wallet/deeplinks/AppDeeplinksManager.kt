package org.p2p.wallet.deeplinks

import androidx.core.content.getSystemService
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.intercom.IntercomDeeplinkManager
import org.p2p.wallet.notification.NotificationType
import org.p2p.wallet.root.RootActivity
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.utils.toStringMap

class AppDeeplinksManager(
    private val context: Context,
    private val intercomDeeplinkManager: IntercomDeeplinkManager
) {

    companion object {
        const val NOTIFICATION_TYPE = "eventType"
        const val DEEPLINK_MAIN_SCREEN_EXTRA = "DEEPLINK_SCREEN_EXTRA"
    }

    private var mainTabsSwitcher: MainTabsSwitcher? = null
    private var rootListener: RootListener? = null

    private var pendingIntent: Intent? = null

    fun setTabsSwitcher(mainTabsSwitcher: MainTabsSwitcher) {
        this.mainTabsSwitcher = mainTabsSwitcher
    }

    fun setRootListener(rootListener: RootListener) {
        this.rootListener = rootListener
    }

    fun clearTabsSwitcher() {
        mainTabsSwitcher = null
    }

    fun clearRootListener() {
        rootListener = null
    }

    fun handleDeeplinkIntent(intent: Intent) {
        when {
            isDeeplinkWithUri(intent) -> {
                val data = intent.data ?: return

                val isValidScheme = context.getString(R.string.app_scheme) == data.scheme
                when {
                    isValidScheme && DeeplinkUtils.isValidOnboardingLink(data) -> {
                        rootListener?.triggerOnboardingDeeplink(data)
                    }
                    intercomDeeplinkManager.handleBackgroundDeeplink(data) -> {
                        // do nothing
                    }
                }
            }
            isDeeplinkWithExtras(intent) -> {
                val extras = intent.extras ?: return

                when {
                    // additional parsing when app been opened with notification from background
                    extras.containsKey(NOTIFICATION_TYPE) -> {
                        val values = extras.toStringMap()
                        val notificationType = NotificationType.fromValue(
                            values[NOTIFICATION_TYPE].orEmpty()
                        )
                        intent.addDeeplinkDataToIntent(notificationType)
                    }
                    extras.containsKey(DEEPLINK_MAIN_SCREEN_EXTRA) -> {
                        switchToMainTabIfPossible(intent)
                    }
                }
            }
            else -> Unit
        }
    }

    fun buildIntent(notificationType: NotificationType): Intent {
        val activityManager = context.getSystemService<ActivityManager>()
        val openedScreenOrRoot = activityManager?.appTasks
            ?.firstOrNull()
            ?.taskInfo
            ?.baseIntent
            ?: Intent(context, RootActivity::class.java)
        return openedScreenOrRoot.apply {
            addDeeplinkDataToIntent(notificationType)
        }
    }

    fun executeHomePendingDeeplink() {
        pendingIntent?.let { switchToMainTabIfPossible(it) }
        pendingIntent = null
    }

    private fun Intent.addDeeplinkDataToIntent(notificationType: NotificationType) {
        if (notificationType == NotificationType.RECEIVE) {
            putExtra(DEEPLINK_MAIN_SCREEN_EXTRA, R.id.historyItem)
        }
    }

    private fun switchToMainTabIfPossible(intent: Intent) {
        val extras = intent.extras ?: return

        rootListener?.popBackStackToMain()

        val clickedTab = ScreenTab.fromTabId(extras.getInt(DEEPLINK_MAIN_SCREEN_EXTRA))!!
        mainTabsSwitcher?.navigate(clickedTab) ?: savePendingIntent(intent)
    }

    private fun savePendingIntent(intent: Intent) {
        pendingIntent = intent
    }

    private fun isDeeplinkWithUri(intent: Intent): Boolean = intent.data != null

    private fun isDeeplinkWithExtras(intent: Intent): Boolean = intent.extras != null
}

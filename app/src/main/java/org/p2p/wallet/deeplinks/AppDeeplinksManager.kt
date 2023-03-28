package org.p2p.wallet.deeplinks

import androidx.core.content.getSystemService
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.intercom.IntercomDeeplinkManager
import org.p2p.wallet.notification.NotificationType
import org.p2p.wallet.root.RootActivity
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.utils.toStringMap

private const val EXTRA_TAB_SCREEN = "EXTRA_TAB_SCREEN"

class AppDeeplinksManager(
    private val context: Context,
    private val intercomDeeplinkManager: IntercomDeeplinkManager
) {

    companion object {
        const val NOTIFICATION_TYPE = "eventType"
    }

    private var mainTabsSwitcher: MainTabsSwitcher? = null
    private var rootListener: RootListener? = null

    private var pendingIntent: Intent? = null

    private var transferPendingDeeplink: Uri? = null

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
                val isTransferScheme = context.getString(R.string.transfer_app_scheme) == data.scheme
                when {
                    isValidScheme && DeeplinkUtils.isValidOnboardingLink(data) -> {
                        rootListener?.triggerOnboardingDeeplink(data)
                    }
                    isTransferScheme -> {
                        transferPendingDeeplink = data
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
                    extras.containsKey(EXTRA_TAB_SCREEN) -> {
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

        transferPendingDeeplink?.let { deeplink ->
            rootListener?.executeTransferViaLink(deeplink)
        }
    }

    private fun Intent.addDeeplinkDataToIntent(notificationType: NotificationType) {
        val navigationId = when (notificationType) {
            NotificationType.RECEIVE -> R.id.historyItem
            NotificationType.DEFAULT -> R.id.homeItem
        }

        putExtra(EXTRA_TAB_SCREEN, navigationId)
    }

    private fun switchToMainTabIfPossible(intent: Intent) {
        val extras = intent.extras ?: return

        rootListener?.popBackStackToMain()

        val clickedTab = ScreenTab.fromTabId(extras.getInt(EXTRA_TAB_SCREEN))!!
        mainTabsSwitcher?.navigate(clickedTab) ?: savePendingIntent(intent)
    }

    private fun savePendingIntent(intent: Intent) {
        pendingIntent = intent
    }

    private fun isDeeplinkWithUri(intent: Intent): Boolean = intent.data != null

    private fun isDeeplinkWithExtras(intent: Intent): Boolean = intent.extras != null
}

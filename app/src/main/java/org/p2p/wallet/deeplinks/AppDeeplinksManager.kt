package org.p2p.wallet.deeplinks

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.p2p.uikit.components.ScreenTab
import org.p2p.wallet.R
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.intercom.IntercomDeeplinkManager
import org.p2p.wallet.notification.NotificationType
import org.p2p.wallet.root.RootActivity
import org.p2p.wallet.utils.toStringMap

class AppDeeplinksManager(
    private val context: Context,
    private val intercomDeeplinkManager: IntercomDeeplinkManager
) {

    companion object {
        const val NOTIFICATION_TYPE = "eventType"
        const val DEEPLINK_MAIN_SCREEN_EXTRA = "DEEPLINK_SCREEN_EXTRA"
    }

    var mainTabsSwitcher: MainTabsSwitcher? = null
    var mainFragmentManager: FragmentManager? = null

    private var pendingIntent: Intent? = null
    var pendingDeeplinkUri: Uri? = null

    val hasPendingDeeplink
        get() = pendingDeeplinkUri != null

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

    private fun Intent.addDeeplinkDataToIntent(notificationType: NotificationType) {
        if (notificationType == NotificationType.RECEIVE) {
            putExtra(DEEPLINK_MAIN_SCREEN_EXTRA, R.id.historyItem)
        }
    }

    fun handleDeeplinkIntent(intent: Intent) {
        when {
            isDeeplinkWithUri(intent) -> {
                val data = intent.data ?: return
                if (context.getString(R.string.app_scheme) == data.scheme &&
                    data.host in DeeplinkHosts.validListToStartDeeplinks
                ) {
                    pendingDeeplinkUri = data
                } else {
                    intercomDeeplinkManager.handleBackgroundDeeplink(data)
                }
            }
            isDeeplinkWithExtras(intent) -> {
                val extras = intent.extras ?: return
                // additional parsing when app been opened with notification from background
                if (extras.containsKey(NOTIFICATION_TYPE)) {
                    val values = extras.toStringMap()
                    val notificationType = NotificationType.fromValue(
                        values[NOTIFICATION_TYPE].orEmpty()
                    )
                    intent.addDeeplinkDataToIntent(notificationType)
                }
                handleOrSaveDeeplinkIntent(intent)
            }
            else -> Unit
        }
    }

    private fun isDeeplinkWithUri(intent: Intent): Boolean = intent.data != null

    private fun isDeeplinkWithExtras(intent: Intent): Boolean = intent.extras != null

    private fun handleOrSaveDeeplinkIntent(intent: Intent) {
        intent.extras?.apply {
            if (containsKey(DEEPLINK_MAIN_SCREEN_EXTRA)) {
                popToMainScreen()
                mainTabsSwitcher?.navigate(
                    ScreenTab.fromTabId(getInt(DEEPLINK_MAIN_SCREEN_EXTRA))!!
                )
                    ?: savePendingIntent(intent)
            }
        }
    }

    private fun popToMainScreen() {
        mainFragmentManager?.apply {
            if (backStackEntryCount > 1) {
                val lastScreen = fragments.lastOrNull()
                if (lastScreen is BottomSheetDialogFragment) {
                    lastScreen.dismissAllowingStateLoss()
                }
                popBackStackImmediate(
                    MainFragment::class.java.name,
                    0
                )
            }
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

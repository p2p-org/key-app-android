package org.p2p.wallet.deeplinks

import androidx.core.content.getSystemService
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onCompletion
import org.p2p.wallet.R
import org.p2p.wallet.intercom.IntercomDeeplinkManager
import org.p2p.wallet.notification.NotificationType
import org.p2p.wallet.root.RootActivity
import org.p2p.wallet.root.RootListener
import org.p2p.wallet.svl.interactor.SendViaLinkWrapper
import org.p2p.wallet.utils.toStringMap

private const val EXTRA_TAB_SCREEN = "EXTRA_TAB_SCREEN"

class AppDeeplinksManager(
    private val context: Context,
    private val intercomDeeplinkManager: IntercomDeeplinkManager
) {

    companion object {
        const val NOTIFICATION_TYPE = "eventType"
    }

    private val deeplinkData = MutableStateFlow<DeeplinkData?>(null)

    /**
     * Tabs switcher for navigate by home screen (see [org.p2p.wallet.home.MainFragment])
     */
    private var mainTabsSwitcher: MainTabsSwitcher? = null

    /**
     * Listener for root activity (see [org.p2p.wallet.root.RootActivity])
     */
    private var rootListener: RootListener? = null

    /**
     * Intent for pending home intents, like attempt to
     */
    private var pendingIntent: Intent? = null

    /**
     * Intent for send-via-link
     */
    private var pendingTransferLink: SendViaLinkWrapper? = null

    /**
     * Notify manager that we have new deeplink to handle
     * @param data Deeplink data
     */
    fun notify(data: DeeplinkData) {
        deeplinkData.tryEmit(data)
    }

    /**
     * Get the deeplink data flow for specified targets
     * @param supportedTargets Set of deeplink targets, empty means all targets will be emitted
     */
    fun subscribeOnDeeplinks(supportedTargets: Set<DeeplinkTarget> = emptySet()): Flow<DeeplinkData> {
        return deeplinkData
            .filterNotNull()
            .let { flow ->
                if (supportedTargets.isEmpty()) {
                    flow
                } else {
                    flow.filter { item -> item.target in supportedTargets }
                }
            }
            .onCompletion {
                // dispose after all subscribers are unsubscribed
                deeplinkData.tryEmit(null)
            }
    }

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
                val isTransferScheme = context.getString(R.string.transfer_app_scheme) == data.host
                when {
                    isValidScheme && DeeplinkUtils.isValidOnboardingLink(data) -> {
                        rootListener?.triggerOnboardingDeeplink(data)
                    }
                    isValidScheme && DeeplinkUtils.isValidCommonLink(data) -> {
                        val screenName = data.host
                        val target = DeeplinkTarget.fromScreenName(screenName)
                        target?.let { deeplinkTarget ->
                            val deeplinkData = DeeplinkData(
                                target = deeplinkTarget,
                                pathSegments = data.pathSegments,
                                args = data.queryParameterNames
                                    .filter { !data.getQueryParameter(it).isNullOrBlank() }
                                    .associateWith { data.getQueryParameter(it)!! },
                                intent = intent
                            )
                            notify(deeplinkData)
                        }
                    }
                    isTransferScheme -> {
                        val deeplink = SendViaLinkWrapper(data.toString())
                        val isExecuted = rootListener?.parseTransferViaLink(deeplink) ?: false
                        if (!isExecuted) {
                            // postpone deeplink execution until app will be ready
                            pendingTransferLink = deeplink
                        }
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
    }

    fun executeTransferPendingAppLink() {
        val link = pendingTransferLink ?: return

        if (rootListener?.parseTransferViaLink(link) == true) {
            pendingTransferLink = null
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

        val clickedTab = DeeplinkTarget.fromScreenTabId(extras.getInt(EXTRA_TAB_SCREEN))
        clickedTab?.let { target ->
            val deeplinkData = DeeplinkData(target, intent = intent)
            notify(deeplinkData)
        }
    }

    private fun isDeeplinkWithUri(intent: Intent): Boolean = intent.data != null

    private fun isDeeplinkWithExtras(intent: Intent): Boolean = intent.extras != null
}

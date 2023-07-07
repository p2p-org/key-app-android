package org.p2p.wallet.deeplinks

import androidx.core.content.getSystemService
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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

    fun setRootListener(rootListener: RootListener) {
        this.rootListener = rootListener
    }

    fun clearRootListener() {
        rootListener = null
    }

    fun handleDeeplinkIntent(intent: Intent) {
        when {
            isDeeplinkWithUri(intent) -> {
                val data = intent.data ?: return

                val isValidScheme = context.getString(R.string.app_scheme) == data.scheme
                val isTransferScheme = isTransferDeeplink(data)

                when {
                    isValidScheme && DeeplinkUtils.isValidOnboardingLink(data) -> handleOnboardingDeeplink(data)
                    isValidScheme && DeeplinkUtils.isValidCommonLink(data) -> handleCommonDeeplink(intent)
                    isTransferScheme -> handleTransferDeeplink(data)
                    intercomDeeplinkManager.handleBackgroundDeeplink(data) -> Unit
                }
            }
            isDeeplinkWithExtras(intent) -> handleDeeplinkWithExtras(intent)
            else -> Unit
        }
    }

    /**
     * https://t.key.app/...
     * or keyapp://t/... if came from website
     */
    private fun isTransferDeeplink(data: Uri): Boolean {
        val transferHostMain = context.getString(R.string.transfer_app_host)
        val transferSchemeMain = "https"
        val transferHostAlternative = "t"
        val transferSchemeAlternative = context.getString(R.string.transfer_app_scheme_alternative)
        return data.host == transferHostMain && data.scheme == transferSchemeMain ||
            data.host == transferHostAlternative && data.scheme == transferSchemeAlternative
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
            NotificationType.DEFAULT -> R.id.myCryptoItem
        }

        putExtra(EXTRA_TAB_SCREEN, navigationId)
    }

    private fun handleDeeplinkWithExtras(intent: Intent) {
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

    private fun handleOnboardingDeeplink(data: Uri) {
        rootListener?.triggerOnboardingDeeplink(data)
    }

    private fun handleTransferDeeplink(data: Uri) {
        val validatedSvlLink = if (data.scheme == context.getString(R.string.transfer_app_scheme_alternative)) {
            // convert link to https format
            data.buildUpon()
                .scheme("https")
                .authority("t.key.app")
                .build()
        } else {
            data
        }
        val deeplink = SendViaLinkWrapper(validatedSvlLink.toString())
        val isExecuted = rootListener?.parseTransferViaLink(deeplink) ?: false
        if (!isExecuted) {
            // postpone deeplink execution until app will be ready
            pendingTransferLink = deeplink
        }
    }

    private fun handleCommonDeeplink(intent: Intent) {
        val data = intent.data ?: return
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

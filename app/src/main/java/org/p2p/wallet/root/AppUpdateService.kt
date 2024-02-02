package org.p2p.wallet.root

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.requestAppUpdateInfo
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.feature_toggles.toggles.remote.ForceUpdateVersionCodeFeatureToggle
import org.p2p.wallet.infrastructure.coroutines.waitForCondition

class AppUpdateService(
    private val context: Context,
    private val forceUpdateVersionCodeFt: ForceUpdateVersionCodeFeatureToggle
) {
    private suspend fun isUpdateNeeded(): Boolean {
        val isForceUpdateCodeFetched = waitForCondition(2.seconds.inWholeMilliseconds) {
            forceUpdateVersionCodeFt.value != -1
        }
        if (!isForceUpdateCodeFetched) {
            Timber.e(IllegalArgumentException("Force update version is not fetched"))
            return false
        }

        val forceUpdateVersionCode = forceUpdateVersionCodeFt.value

        val appUpdateInfo = AppUpdateManagerFactory.create(context).requestAppUpdateInfo()
        val currentVersionCode = BuildConfig.VERSION_CODE
        val storeVersionCode = appUpdateInfo.availableVersionCode()

        Timber.i(
            buildString {
                appendLine("storeVersionCode = $storeVersionCode, ")
                appendLine("currentVersionCode = $currentVersionCode, ")
                appendLine("forceUpdateVersionCode = $forceUpdateVersionCode")
            }
        )

        val isForceUpdateNeeded = currentVersionCode < forceUpdateVersionCode
        val isForceUpdateAvailableInStore = storeVersionCode >= forceUpdateVersionCode

        val isUpdatePossible = appUpdateInfo.isImmediateUpdateAllowed

        return isUpdatePossible && isForceUpdateAvailableInStore && isForceUpdateNeeded
    }

    suspend fun startInAppUpdateIfNeeded(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        if (!isUpdateNeeded()) return

        val appUpdateManager = AppUpdateManagerFactory.create(context)
        val appUpdateInfo = appUpdateManager.requestAppUpdateInfo()
        val immediateUpdate = AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)

        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            activityResultLauncher,
            immediateUpdate
        )
    }

    suspend fun checkUpdateIsNotStalled(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val appUpdateManager = AppUpdateManagerFactory.create(context)
        val appUpdateInfo = appUpdateManager.requestAppUpdateInfo()
        val isStalled = appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        if (isStalled) {
            startInAppUpdateIfNeeded(activityResultLauncher)
        }
    }
}

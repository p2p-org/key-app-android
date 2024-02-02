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
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.feature_toggles.toggles.remote.ForceUpdateVersionCodeFeatureToggle

class AppUpdateService(
    private val context: Context,
    private val forceUpdateVersionCodeFt: ForceUpdateVersionCodeFeatureToggle
) {
    private suspend fun isUpdateNeeded(): Boolean {
        val forceUpdateVersionCode = forceUpdateVersionCodeFt.value
        if (forceUpdateVersionCode == -1) {
            Timber.e(IllegalArgumentException("Force update version is not fetched"))
            return false
        }

        val appUpdateInfo = AppUpdateManagerFactory.create(context).requestAppUpdateInfo()
        val currentVersionCode = BuildConfig.VERSION_CODE
        val storeVersionCode = appUpdateInfo.availableVersionCode()

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

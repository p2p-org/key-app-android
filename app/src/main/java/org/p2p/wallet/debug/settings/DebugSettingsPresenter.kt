package org.p2p.wallet.debug.settings

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import kotlinx.coroutines.launch
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.renbtc.service.RenVMService
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.utils.appendBreakLine
import timber.log.Timber

class DebugSettingsPresenter(
    private val environmentManager: NetworkEnvironmentManager,
    private val homeLocalRepository: HomeLocalRepository,
    private val context: Context,
    private val resourcesProvider: ResourcesProvider,
    networkServicesUrlProvider: NetworkServicesUrlProvider
) : BasePresenter<DebugSettingsContract.View>(), DebugSettingsContract.Presenter {

    private var networkName = environmentManager.loadCurrentEnvironment().name
    private val feeRelayerUrl = networkServicesUrlProvider.loadFeeRelayerEnvironment().baseUrl
    private val notificationServiceUrl = networkServicesUrlProvider.loadNotificationServiceEnvironment().baseUrl
    private val torusUrl = networkServicesUrlProvider.loadTorusEnvironment().baseUrl

    override fun loadData() {
        val settings = getMainSettings() + getAppInfoSettings() + getDeviceInfo() + getCiInfo()
        view?.showSettings(settings)
    }

    override fun onNetworkChanged(newNetworkEnvironment: NetworkEnvironment) {
        this.networkName = newNetworkEnvironment.name

        launch {
            try {
                environmentManager.chooseEnvironment(newNetworkEnvironment)
                homeLocalRepository.clear()
                RenVMService.stopService(context)
            } catch (error: Throwable) {
                Timber.e(error, "Network changing failed")
            }
        }

        loadData()
    }

    private fun getMainSettings(): List<SettingsRow> {
        return listOf(
            SettingsRow.Section(
                titleResId = R.string.debug_settings_notifications_title,
                iconRes = R.drawable.ic_settings_notification
            ),
            SettingsRow.Section(
                titleResId = R.string.debug_settings_deeplinks_title,
                iconRes = R.drawable.ic_network
            ),
            SettingsRow.Section(
                titleResId = R.string.settings_network,
                subtitle = networkName,
                iconRes = R.drawable.ic_settings_network
            ),
            SettingsRow.Section(
                titleResId = R.string.settings_fee_relayer,
                subtitle = feeRelayerUrl,
                iconRes = R.drawable.ic_network
            ),
            SettingsRow.Section(
                titleResId = R.string.settings_torus,
                subtitle = torusUrl,
                iconRes = R.drawable.ic_network
            ),
            SettingsRow.Section(
                titleResId = R.string.settings_notification_service,
                subtitle = notificationServiceUrl,
                iconRes = R.drawable.ic_network
            ),
            SettingsRow.Section(
                titleResId = R.string.debug_settings_feature_toggles_title,
                iconRes = R.drawable.ic_home_settings
            ),
            SettingsRow.Section(
                titleResId = R.string.debug_settings_logs_title,
                subtitle = resourcesProvider.getString(R.string.debug_settings_logs_subtitle),
                iconRes = R.drawable.ic_settings_cloud
            )
        )
    }

    private fun getDeviceInfo(): List<SettingsRow> {
        val deviceValues = buildString {
            val displayMetrics: DisplayMetrics = resourcesProvider.resources.displayMetrics
            val densityBucket = getDensityString(displayMetrics)
            val deviceMake = truncateAt(Build.MANUFACTURER, 20)
            val deviceModel = truncateAt(Build.MODEL, 20)
            val deviceResolution = displayMetrics.heightPixels.toString() + "x" + displayMetrics.widthPixels
            val deviceDensity = displayMetrics.densityDpi.toString() + "dpi (" + densityBucket + ")"
            val deviceRelease = Build.VERSION.RELEASE
            val deviceApi = Build.VERSION.SDK_INT.toString()
            createRecord("densityBucket", densityBucket)
            createRecord("deviceMake", deviceMake)
            createRecord("deviceModel", deviceModel)
            createRecord("deviceResolution", deviceResolution)
            createRecord("deviceDensity", deviceDensity)

            appendBreakLine()

            createRecord("deviceRelease", deviceRelease)
            createRecord("deviceApi", deviceApi)
        }
        return listOf(
            SettingsRow.Info(
                R.string.debug_settings_device_info,
                subtitle = deviceValues
            ),
        )
    }

    private fun getCiInfo(): List<SettingsRow> {
        val ciValues = buildString {
            createApiKeyRecord("amplitudeKey", BuildConfig.amplitudeKey)
            createApiKeyRecord("comparePublicKey", BuildConfig.comparePublicKey)
            createApiKeyRecord("intercomApiKey", BuildConfig.intercomApiKey)
            createApiKeyRecord("intercomAppId", BuildConfig.intercomAppId)
            createApiKeyRecord("moonpayKey", BuildConfig.moonpayKey)
            createApiKeyRecord("rpcPoolApiKey", BuildConfig.rpcPoolApiKey)

            appendBreakLine()

            createFlagRecord("AMPLITUDE_ENABLED", BuildConfig.AMPLITUDE_ENABLED)
            createFlagRecord("CRASHLYTICS_ENABLED", BuildConfig.CRASHLYTICS_ENABLED)
            createFlagRecord("KEY_DEV_NET_ENABLED", BuildConfig.KEY_DEV_NET_ENABLED)
        }
        return listOf(
            SettingsRow.Info(
                R.string.debug_settings_ci_info,
                subtitle = ciValues
            ),
        )
    }

    private fun getAppInfoSettings(): List<SettingsRow> {
        return listOf(
            SettingsRow.Title(R.string.debug_settings_app_info, isDivider = true),
            SettingsRow.Section(
                titleResId = R.string.settings_app_version,
                subtitle = "${BuildConfig.BUILD_TYPE}-${BuildConfig.VERSION_NAME}",
                iconRes = R.drawable.ic_settings_app_version
            )
        )
    }

    private fun StringBuilder.createApiKeyRecord(apiKeyName: String, apiKey: String) {
        append("$apiKeyName = ")
        append("***")
        append(apiKey.removeRange(startIndex = 0, endIndex = apiKey.length - 3))
        appendBreakLine()
    }

    private fun StringBuilder.createRecord(flagName: String, flagValue: String) {
        append("$flagName = $flagValue")
        appendBreakLine()
    }

    private fun StringBuilder.createFlagRecord(flagName: String, flagValue: Boolean) {
        append("$flagName = $flagValue")
        appendBreakLine()
    }

    private fun truncateAt(string: String, length: Int): String {
        return if (string.length > length) string.substring(0, length) else string
    }

    private fun getDensityString(displayMetrics: DisplayMetrics): String {
        return when (displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> "ldpi"
            DisplayMetrics.DENSITY_MEDIUM -> "mdpi"
            DisplayMetrics.DENSITY_HIGH -> "hdpi"
            DisplayMetrics.DENSITY_XHIGH -> "xhdpi"
            DisplayMetrics.DENSITY_XXHIGH -> "xxhdpi"
            DisplayMetrics.DENSITY_XXXHIGH -> "xxxhdpi"
            DisplayMetrics.DENSITY_TV -> "tvdpi"
            else -> displayMetrics.densityDpi.toString()
        }
    }
}

package org.p2p.wallet.debug.settings

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.network.environment.NetworkEnvironment
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.wallet.R
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.renbtc.service.RenVMService
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.utils.appendBreakLine
import org.p2p.core.BuildConfig as CoreBuildConfig
import org.p2p.wallet.BuildConfig as AppBuildConfig

class DebugSettingsPresenter(
    private val interactor: DebugSettingsInteractor,
    private val environmentManager: NetworkEnvironmentManager,
    private val homeLocalRepository: HomeLocalRepository,
    private val context: Context,
    private val resources: Resources,
    private val networkServicesUrlProvider: NetworkServicesUrlProvider,
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val appRestarter: AppRestarter,
    private val mapper: DebugSettingsMapper,

) : BasePresenter<DebugSettingsContract.View>(), DebugSettingsContract.Presenter {

    private var networkName = environmentManager.loadCurrentEnvironment().name

    override fun loadData() {
        val settings = mapper.mapMainSettings() + getAppInfoSettings() + getDeviceInfo() + getCiInfo()
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

    override fun switchMoonpayUrl(isSandboxSelected: Boolean) {
        networkServicesUrlProvider.toggleMoonpayEnvironment(isSandboxSelected)
        loadData()
    }

    override fun switchNameServiceUrl(isProdSelected: Boolean) {
        networkServicesUrlProvider.toggleNameServiceEnvironment(isProdSelected)
        appRestarter.restartApp()
    }

    override fun onSettingsPopupMenuClicked(selectedValue: String) {
        if (selectedValue != "-") {
            inAppFeatureFlags.strigaKycBannerMockFlag.featureValueString = selectedValue
        } else {
            inAppFeatureFlags.strigaKycBannerMockFlag.featureValueString = null
        }
    }

    override fun onClickSetKycRejected() {
        launch {
            try {
                interactor.setStrigaKycRejected()
                view?.showUiKitSnackBar("Status successfully changed")
            } catch (e: Throwable) {
                Timber.d(e)
                view?.showErrorMessage(e)
            }
        }
    }

    override fun onClickDetachStrigaUser() {
        launch {
            try {
                interactor.detachStrigaUserFromMetadata()
                view?.showUiKitSnackBar("Striga user is successfully detached")
                appRestarter.restartApp()
            } catch (e: Throwable) {
                view?.showUiKitSnackBar("Metadata is not loaded. Unable to proceed.")
            }
        }
    }

    private fun getDeviceInfo(): List<SettingsRow> {
        val deviceValues = buildString {
            val displayMetrics: DisplayMetrics = resources.displayMetrics
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
            createApiKeyRecord("amplitudeKey", AppBuildConfig.amplitudeKey)
            createApiKeyRecord("intercomApiKey", CoreBuildConfig.intercomApiKey)
            createApiKeyRecord("intercomAppId", CoreBuildConfig.intercomAppId)
            createApiKeyRecord("moonpayKey", CoreBuildConfig.moonpayKey)
            createApiKeyRecord("moonpaySanbdoxKey", CoreBuildConfig.moonpaySandboxKey)
            createApiKeyRecord("rpcPoolApiKey", CoreBuildConfig.rpcPoolApiKey)

            appendBreakLine()

            createFlagRecord("CRASHLYTICS_ENABLED", CoreBuildConfig.CRASHLYTICS_ENABLED)
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
                subtitle = "${AppBuildConfig.BUILD_TYPE}-${AppBuildConfig.VERSION_NAME}",
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

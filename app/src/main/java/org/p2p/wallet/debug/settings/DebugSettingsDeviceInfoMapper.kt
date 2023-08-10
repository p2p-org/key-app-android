package org.p2p.wallet.debug.settings

import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.utils.appendBreakLine
import org.p2p.core.BuildConfig as CoreBuildConfig

class DebugSettingsDeviceInfoMapper(
    private val resources: Resources
) {
    fun mapDeviceInfo(): List<SettingsRow.Info> {
        val deviceValues = buildString {
            val displayMetrics: DisplayMetrics = resources.displayMetrics
            val densityBucket = getDensityString(displayMetrics)
            val deviceMake = truncateAt(Build.MANUFACTURER, 20)
            val deviceModel = truncateAt(Build.MODEL, 20)
            val deviceResolution = displayMetrics.heightPixels.toString() + "x" + displayMetrics.widthPixels
            val deviceDensity = displayMetrics.densityDpi.toString() + "dpi (" + densityBucket + ")"
            val deviceRelease = Build.VERSION.RELEASE
            val deviceApi = Build.VERSION.SDK_INT.toString()
            appendFlagRecord("densityBucket", densityBucket)
            appendFlagRecord("deviceMake", deviceMake)
            appendFlagRecord("deviceModel", deviceModel)
            appendFlagRecord("deviceResolution", deviceResolution)
            appendFlagRecord("deviceDensity", deviceDensity)

            appendBreakLine()

            appendFlagRecord("deviceRelease", deviceRelease)
            appendFlagRecord("deviceApi", deviceApi)
        }
        return listOf(
            SettingsRow.Info(
                R.string.debug_settings_device_info,
                subtitle = deviceValues
            )
        )
    }

    fun mapCiInfo(): List<SettingsRow> {
        val ciValues = buildString {
            appendApiKeyRecord("amplitudeKey", BuildConfig.amplitudeKey)
            appendApiKeyRecord("intercomApiKey", CoreBuildConfig.intercomApiKey)
            appendApiKeyRecord("intercomAppId", CoreBuildConfig.intercomAppId)
            appendApiKeyRecord("moonpayKey", CoreBuildConfig.moonpayKey)
            appendApiKeyRecord("moonpaySanbdoxKey", CoreBuildConfig.moonpaySandboxKey)
            appendApiKeyRecord("rpcPoolApiKey", CoreBuildConfig.rpcPoolApiKey)
            appendApiKeyRecord("lokaliseKey", CoreBuildConfig.lokaliseKey)
            appendApiKeyRecord("lokaliseAppId", CoreBuildConfig.lokaliseAppId)
            appendApiKeyRecord("appsFlyerKey", CoreBuildConfig.appsFlyerKey)

            appendBreakLine()

            appendFlagRecord("CRASHLYTICS_ENABLED", CoreBuildConfig.CRASHLYTICS_ENABLED)
            appendFlagRecord("CRASHLYTICS_ENABLED", CoreBuildConfig.APPSFLYER_ENABLED)
            appendFlagRecord("CRASHLYTICS_ENABLED", CoreBuildConfig.SENTRY_ENABLED)
            appendFlagRecord("CRASHLYTICS_ENABLED", CoreBuildConfig.SENTRY_ENABLED)
        }
        return listOf(
            SettingsRow.Info(
                R.string.debug_settings_ci_info,
                subtitle = ciValues
            ),
        )
    }

    fun mapAppInfo(): List<SettingsRow> {
        return listOf(
            SettingsRow.Title(R.string.debug_settings_app_info, isDivider = true),
            SettingsRow.Section(
                titleResId = R.string.settings_app_version,
                subtitle = "${BuildConfig.BUILD_TYPE}-${BuildConfig.VERSION_NAME}",
                iconRes = R.drawable.ic_settings_app_version
            )
        )
    }

    private fun StringBuilder.appendApiKeyRecord(apiKeyName: String, apiKey: String) {
        append("$apiKeyName = ")
        append("***")
        append(apiKey.removeRange(startIndex = 0, endIndex = apiKey.length - 3))
        appendBreakLine()
    }

    private fun StringBuilder.appendFlagRecord(flagName: String, flagValue: Boolean) {
        appendFlagRecord(flagName, flagValue.toString())
    }

    private fun StringBuilder.appendFlagRecord(flagName: String, flagValue: String) {
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

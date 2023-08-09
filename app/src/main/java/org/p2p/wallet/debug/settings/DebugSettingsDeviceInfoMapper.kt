package org.p2p.wallet.debug.settings

import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.utils.appendBreakLine

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
            )
        )
    }

    fun mapCiInfo(): List<SettingsRow> {
        val ciValues = buildString {
            createApiKeyRecord("amplitudeKey", BuildConfig.amplitudeKey)
            createApiKeyRecord("intercomApiKey", org.p2p.core.BuildConfig.intercomApiKey)
            createApiKeyRecord("intercomAppId", org.p2p.core.BuildConfig.intercomAppId)
            createApiKeyRecord("moonpayKey", org.p2p.core.BuildConfig.moonpayKey)
            createApiKeyRecord("moonpaySanbdoxKey", org.p2p.core.BuildConfig.moonpaySandboxKey)
            createApiKeyRecord("rpcPoolApiKey", org.p2p.core.BuildConfig.rpcPoolApiKey)

            appendBreakLine()

            createFlagRecord("CRASHLYTICS_ENABLED", org.p2p.core.BuildConfig.CRASHLYTICS_ENABLED)
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

    private fun StringBuilder.createApiKeyRecord(apiKeyName: String, apiKey: String) {
        append("$apiKeyName = ")
        append("***")
        append(apiKey.removeRange(startIndex = 0, endIndex = apiKey.length - 3))
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

    private fun StringBuilder.createRecord(flagName: String, flagValue: String) {
        append("$flagName = $flagValue")
        appendBreakLine()
    }
}

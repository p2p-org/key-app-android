package org.p2p.wallet.common.feature_toggles.remote_config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.launch
import timber.log.Timber
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.di.AppScope

private const val NO_VALUE = ""

class AppFirebaseRemoteConfig(
    private val appScope: AppScope
) : RemoteConfigValuesProvider {

    private class RemoteConfigError(
        override val message: String,
        override val cause: Throwable
    ) : Throwable()

    private var isFetchFailed: Boolean = false

    private val remoteConfig: FirebaseRemoteConfig
        get() = FirebaseRemoteConfig.getInstance()

    fun loadRemoteConfig(onConfigLoaded: (featureToggles: Map<String, String>) -> Unit) {
        appScope.launch {
            // do not put it in release - we don't need to change fetch interval there
            if (BuildConfig.DEBUG) {
                remoteConfig.setConfigSettingsAsync(createRemoteConfigSettings())
                    .addOnSuccessListener { Timber.d("Remote config for debug is set") }
                    .addOnFailureListener { Timber.e("Remote config for debug is not set", it) }
            }

            // maybe add loading screen in the start for remoteConfig to finish the job?
            remoteConfig.fetchAndActivate()
                .addOnSuccessListener {
                    isFetchFailed = false
                    Timber.d("Remote config fetched and activated")
                    Timber.i("Remote config fetched toggles: ${allFeatureTogglesRaw()}")

                    onConfigLoaded.invoke(allFeatureTogglesRaw())
                }
                .addOnFailureListener { error ->
                    isFetchFailed = true
                    Timber.e(RemoteConfigError("Remote config is not fetched and activated", error))

                    onConfigLoaded.invoke(emptyMap())
                }
        }
    }

    fun allFeatureTogglesRaw(): Map<String, String> = remoteConfig.all.mapValues { it.value.asString() }

    /**
     * The default minimum fetch interval for Remote Config is 12 hours, which means that configs won't be fetched from
     * the backend more than once in a 12 hour window, regardless of how many fetch calls are actually made.
     * Specifically, the minimum fetch interval is determined in this following order:
     * - The parameter in `fetch(long)`
     * - The parameter in `FirebaseRemoteConfigSettings.setMinimumFetchIntervalInSeconds(long)`
     * - The default value of 12 hours
     *
     * To set the minimum fetch interval to a custom value, use `setMinimumFetchIntervalInSeconds(long)`.
     */
    private fun createRemoteConfigSettings(): FirebaseRemoteConfigSettings =
        FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()

    override fun getString(toggleKey: String): String? {
        if (isFetchFailed) {
            return null
        }
        return remoteConfig.getString(toggleKey).takeIf(String::isNotBlank)
    }

    override fun getBoolean(toggleKey: String): Boolean? {
        if (isFetchFailed) {
            return false
        }
        return if (isRemoteValueExists(toggleKey)) remoteConfig.getBoolean(toggleKey) else null
    }

    override fun getInt(toggleKey: String): Int? {
        if (isFetchFailed) {
            return null
        }
        return remoteConfig.getString(toggleKey).toIntOrNull()
    }

    override fun getFloat(toggleKey: String): Float? {
        if (isFetchFailed) {
            return null
        }
        return remoteConfig.getString(toggleKey).toFloatOrNull()
    }

    private fun isRemoteValueExists(toggleKey: String): Boolean {
        return remoteConfig.getString(toggleKey) != NO_VALUE
    }
}

package org.p2p.wallet.common.feature_toggles.remote_config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import timber.log.Timber
import kotlin.IllegalStateException

class AppFirebaseRemoteConfig : RemoteConfigValuesProvider {

    private var isFetchFailed: Boolean = false

    init {
        remoteConfig.setConfigSettingsAsync(createRemoteConfigSettings())
            .addOnSuccessListener { Timber.d("Remote config is set") }
            .addOnFailureListener { Timber.e("Remote config is not set", it) }

        // maybe add loading screen in the start for remoteConfig to finish the job?
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                isFetchFailed = false
                Timber.d("Remote config fetched and activated")
                Timber.i("Remote config fetched toggles: ${allFeatureTogglesRaw()}")
            }
            .addOnFailureListener { error ->
                isFetchFailed = true
                Timber.e(IllegalStateException(message = "Remote config is not fetched and activated", cause = error))
            }
    }

    private val remoteConfig: FirebaseRemoteConfig
        get() = FirebaseRemoteConfig.getInstance()

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

    override fun getBoolean(toggleKey: String): Boolean {
        if (isFetchFailed) {
            return false
        }
        return remoteConfig.getBoolean(toggleKey)
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
}

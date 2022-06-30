package org.p2p.wallet.common.feature_toggles.remote_config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import timber.log.Timber

class AppFirebaseRemoteConfig : RemoteConfigValuesProvider {

    init {
        remoteConfig.setConfigSettingsAsync(createRemoteConfigSettings())
            .addOnSuccessListener { Timber.d("Remote config is set") }
            .addOnFailureListener { Timber.e("Remote config is not set", it) }

        // maybe add loading screen in the start for remoteConfig to finish the job?
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                Timber.d("Remote config fetched and activated")
                Timber.d("Remote config fetched toggles: ${allFeatureTogglesRaw()}")
            }
            .addOnFailureListener { Timber.e("Remote config is not fetched and activated", it) }
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

    override fun getString(toggleKey: String): String? = remoteConfig.getString(toggleKey).takeIf(String::isNotBlank)
    override fun getBoolean(toggleKey: String): Boolean = remoteConfig.getBoolean(toggleKey)
    override fun getInt(toggleKey: String): Int? = remoteConfig.getString(toggleKey).toIntOrNull()
    override fun getFloat(toggleKey: String): Float? = remoteConfig.getString(toggleKey).toFloatOrNull()
}

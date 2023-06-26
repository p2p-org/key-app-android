package org.p2p.wallet.common.feature_toggles.remote_config

import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.InAppFeatureFlags

class FeatureTogglesValuesSource(
    private val debugRemoteConfigValuesSource: LocalFirebaseRemoteConfig,
    private val appFirebaseRemoteConfig: AppFirebaseRemoteConfig,
    private val inAppFeatureFlags: InAppFeatureFlags
) : RemoteConfigValuesProvider {
    private val shouldGetValuesFromDebug
        get() = BuildConfig.DEBUG && inAppFeatureFlags.isDebugRemoteConfigEnabled.featureValue

    private val sourceToGetFrom: RemoteConfigValuesProvider
        get() = if (shouldGetValuesFromDebug) debugRemoteConfigValuesSource else appFirebaseRemoteConfig

    override fun getString(toggleKey: String): String? = sourceToGetFrom.getString(toggleKey)
    override fun getBoolean(toggleKey: String): Boolean? = sourceToGetFrom.getBoolean(toggleKey)
    override fun getFloat(toggleKey: String): Float? = sourceToGetFrom.getFloat(toggleKey)
    override fun getInt(toggleKey: String): Int? = sourceToGetFrom.getInt(toggleKey)
    override fun getLong(toggleKey: String): Long? = sourceToGetFrom.getLong(toggleKey)
}

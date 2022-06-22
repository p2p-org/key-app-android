package org.p2p.wallet.common.feature_toggles.remote_config

import org.p2p.wallet.common.AppFeatureFlags

class FeatureTogglesValuesSourceChooser(
    private val debugRemoteConfigValuesSource: LocalFirebaseRemoteConfig,
    private val appFirebaseRemoteConfig: AppFirebaseRemoteConfig,
    private val appFeatureFlags: AppFeatureFlags
) : RemoteConfigValuesSource {
    private val shouldGetValuesFromDebug
        get() = appFeatureFlags.isDebugRemoteConfigEnabled

    private val sourceToGetFrom: RemoteConfigValuesSource
        get() = if (shouldGetValuesFromDebug) debugRemoteConfigValuesSource else appFirebaseRemoteConfig

    override fun getString(toggleKey: String): String? = sourceToGetFrom.getString(toggleKey)
    override fun getBoolean(toggleKey: String): Boolean = sourceToGetFrom.getBoolean(toggleKey)
    override fun getFloat(toggleKey: String): Float? = sourceToGetFrom.getFloat(toggleKey)
    override fun getInt(toggleKey: String): Int? = sourceToGetFrom.getInt(toggleKey)
}

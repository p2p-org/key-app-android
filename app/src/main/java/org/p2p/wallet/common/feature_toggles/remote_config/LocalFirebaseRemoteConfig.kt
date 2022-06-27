package org.p2p.wallet.common.feature_toggles.remote_config

class LocalFirebaseRemoteConfig(appFirebaseRemoteConfig: AppFirebaseRemoteConfig) : RemoteConfigValuesProvider {

    private val toggleKeyToValue: MutableMap<String, String> =
        appFirebaseRemoteConfig.allFeatureTogglesRaw().toMutableMap()

    fun changeFeatureToggle(toggleKey: String, newValue: String) {
        toggleKeyToValue[toggleKey] = newValue
    }

    override fun getBoolean(toggleKey: String): Boolean = toggleKeyToValue[toggleKey].toBoolean()
    override fun getString(toggleKey: String): String? = toggleKeyToValue[toggleKey]
    override fun getInt(toggleKey: String): Int? = toggleKeyToValue[toggleKey]?.toIntOrNull()
    override fun getFloat(toggleKey: String): Float? = toggleKeyToValue[toggleKey]?.toFloatOrNull()
}

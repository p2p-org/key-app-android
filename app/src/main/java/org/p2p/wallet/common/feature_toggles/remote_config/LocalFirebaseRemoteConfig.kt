package org.p2p.wallet.common.feature_toggles.remote_config

class LocalFirebaseRemoteConfig(
    appFirebaseRemoteConfig: AppFirebaseRemoteConfig,
    private val storage: LocalFeatureToggleStorage
) : RemoteConfigValuesProvider {

    init {
        appFirebaseRemoteConfig.allFeatureTogglesRaw()
            .filterKeys { it !in storage } // do not rewrite already written keys
            .forEach { storage.putFeatureToggle(it.key, it.value) }
    }

    fun changeFeatureToggle(toggleKey: String, newValue: String) {
        storage.putFeatureToggle(toggleKey, newValue)
    }

    override fun getBoolean(toggleKey: String): Boolean = storage[toggleKey].toBoolean()
    override fun getString(toggleKey: String): String? = storage[toggleKey]
    override fun getInt(toggleKey: String): Int? = storage[toggleKey]?.toIntOrNull()
    override fun getLong(toggleKey: String): Long? = storage[toggleKey]?.toLongOrNull()
    override fun getFloat(toggleKey: String): Float? = storage[toggleKey]?.toFloatOrNull()
}

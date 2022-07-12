package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider

sealed class RemoteFeatureToggle<ValueType> {
    /**
     * Toggle key from Firebase RemoteConfig
     */
    abstract val featureKey: String

    /**
     * Human description of what feature toggle is and where is used
     */
    abstract val featureDescription: String

    abstract val value: ValueType
    protected abstract val defaultValue: ValueType
}

abstract class BooleanFeatureToggle(
    private val valuesProvider: RemoteConfigValuesProvider
) : RemoteFeatureToggle<Boolean>() {
    override val value: Boolean get() = valuesProvider.getBoolean(featureKey)
    val isFeatureEnabled: Boolean get() = value
}

/**
 * Use subclasses to override how should be JSON parsed
 */
abstract class JsonFeatureToggle<GsonClass>(
    protected val valuesProvider: RemoteConfigValuesProvider
) : RemoteFeatureToggle<GsonClass>()

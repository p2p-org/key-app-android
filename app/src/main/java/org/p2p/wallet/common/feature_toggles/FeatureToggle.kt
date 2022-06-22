package org.p2p.wallet.common.feature_toggles

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesSource

sealed class FeatureToggle<VALUE_TYPE> {
    /**
     * Toggle key from Firebase RemoteConfig
     */
    abstract val toggleKey: String

    /**
     * Human description of what feature toggle is and where is used
     */
    abstract val toggleDescription: String

    abstract val value: VALUE_TYPE
    protected abstract val defaultValue: VALUE_TYPE
}

abstract class StringFeatureToggle(
    private val valuesProvider: RemoteConfigValuesSource
) : FeatureToggle<String>() {
    override val value: String get() = valuesProvider.getString(toggleKey) ?: defaultValue
}

abstract class BooleanFeatureToggle(
    private val valuesProvider: RemoteConfigValuesSource
) : FeatureToggle<Boolean>() {
    override val value: Boolean get() = valuesProvider.getBoolean(toggleKey)
}

abstract class IntFeatureToggle(
    private val valuesProvider: RemoteConfigValuesSource
) : FeatureToggle<Int>() {
    override val value: Int get() = valuesProvider.getInt(toggleKey) ?: defaultValue
}

abstract class FloatFeatureToggle(
    private val valuesProvider: RemoteConfigValuesSource
) : FeatureToggle<Float>() {
    override val value: Float get() = valuesProvider.getFloat(toggleKey) ?: defaultValue
}

/**
 * Use subclasses to override how should be JSON parsed
 */
abstract class JsonFeatureToggle<GSON_CLASS>(
    protected val valuesProvider: RemoteConfigValuesSource
) : FeatureToggle<GSON_CLASS>()

package org.p2p.wallet.common.feature_toggles.toggles.remote

import timber.log.Timber
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
    override val value: Boolean
        get() = valuesProvider.getBoolean(featureKey) ?: kotlin.run {
            Timber.tag("BooleanFeatureToggle").i("No value found for $featureKey; using defaults = $defaultValue")
            defaultValue
        }
    val isFeatureEnabled: Boolean get() = value
}

abstract class IntFeatureToggle(
    private val valuesProvider: RemoteConfigValuesProvider
) : RemoteFeatureToggle<Int>() {
    override val value: Int
        get() = valuesProvider.getInt(featureKey) ?: kotlin.run {
            Timber.tag("IntFeatureToggle").i("No value found for $featureKey; using defaults = $defaultValue")
            defaultValue
        }
}

abstract class LongFeatureToggle(
    private val valuesProvider: RemoteConfigValuesProvider
) : RemoteFeatureToggle<Long>() {
    override val value: Long
        get() = valuesProvider.getLong(featureKey) ?: kotlin.run {
            Timber.tag("LongFeatureToggle").i("No value found for $featureKey; using defaults = $defaultValue")
            defaultValue
        }
}

abstract class StringFeatureToggle(
    private val valuesProvider: RemoteConfigValuesProvider
) : RemoteFeatureToggle<String>() {
    override val value: String
        get() = valuesProvider.getString(featureKey) ?: kotlin.run {
            Timber.tag("StringFeatureToggle").i("No value found for $featureKey; using defaults = $defaultValue")
            defaultValue
        }
}

/**
 * Use subclasses to override how should be JSON parsed
 */
abstract class JsonFeatureToggle<GsonClass>(
    protected val valuesProvider: RemoteConfigValuesProvider
) : RemoteFeatureToggle<GsonClass>()

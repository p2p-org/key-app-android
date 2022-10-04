package org.p2p.wallet.common.feature_toggles.toggles.remote

import org.p2p.wallet.common.feature_toggles.remote_config.RemoteConfigValuesProvider
import timber.log.Timber

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
            Timber.i("No value found for $featureKey; using defaults = $defaultValue")
            defaultValue
        }
    val isFeatureEnabled: Boolean get() = value
}

/**
 * Use subclasses to override how should be JSON parsed
 */
abstract class JsonFeatureToggle<GsonClass>(
    protected val valuesProvider: RemoteConfigValuesProvider
) : RemoteFeatureToggle<GsonClass>()

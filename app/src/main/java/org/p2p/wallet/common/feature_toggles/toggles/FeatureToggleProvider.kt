package org.p2p.wallet.common.feature_toggles.toggles

import org.p2p.wallet.common.feature_toggles.toggles.remote.RemoteFeatureToggle

/**
 * Provider made to use one class except of providing every FT in other class
 * Usage example:
 * ```
 * val toggle: Toggle = featureToggleProvider.getFeatureToggle(Toggle::class.java)
 * ```
 */
class FeatureToggleProvider(private val featureToggles: List<RemoteFeatureToggle<*>>) {
    @Throws(ClassCastException::class)
    fun <T : RemoteFeatureToggle<*>> getFeatureToggle(klass: Class<T>): T {
        return featureToggles.filterIsInstance(klass).first()
    }
}

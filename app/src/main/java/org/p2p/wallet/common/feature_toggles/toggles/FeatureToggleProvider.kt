package org.p2p.wallet.common.feature_toggles.toggles

import kotlin.reflect.KClass
import org.p2p.wallet.common.feature_toggles.toggles.remote.RemoteFeatureToggle

/**
 * Provider made to use one class except of providing every FT in other class
 * Usage example:
 * ```
 * val toggle: Toggle = featureToggleProvider.getFeatureToggle(Toggle::class.java)
 * val toggle: Toggle = featureToggleProvider.getFeatureToggle<Toggle>()
 * val toggle: Toggle = featureToggleProvider[Toggle::class]
 * ```
 */
class FeatureToggleProvider(private val featureToggles: List<RemoteFeatureToggle<*>>) {
    @Throws(ClassCastException::class)
    fun <T : RemoteFeatureToggle<*>> getFeatureToggle(klass: KClass<T>): T {
        return featureToggles.filterIsInstance(klass.java).first()
    }

    // usage: val ft = ftProvider[StrigaSignupEnabledFeatureToggle::class]
    @Throws(ClassCastException::class)
    operator fun <T : RemoteFeatureToggle<*>> get(klass: KClass<T>): T {
        return getFeatureToggle(klass)
    }

    inline fun <reified T : RemoteFeatureToggle<*>> getFeatureToggle(): T {
        return getFeatureToggle(T::class)
    }
}

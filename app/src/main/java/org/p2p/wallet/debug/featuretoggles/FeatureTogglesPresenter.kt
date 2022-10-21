package org.p2p.wallet.debug.featuretoggles

import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.remote.BooleanFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.RemoteFeatureToggle
import org.p2p.wallet.common.feature_toggles.remote_config.LocalFirebaseRemoteConfig
import org.p2p.wallet.common.mvp.BasePresenter

class FeatureTogglesPresenter(
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val debugRemoteConfigValuesSource: LocalFirebaseRemoteConfig,
    private val remoteFeatureToggles: List<RemoteFeatureToggle<*>>,
) : BasePresenter<FeatureTogglesContract.View>(), FeatureTogglesContract.Presenter {

    override fun load() {
        view?.showFeatureToggles(buildFeatureToggleRows())
    }

    override fun onToggleChanged(toggle: FeatureToggleRow, newValue: String) {
        inAppFeatureFlags.findFeatureFlagByName(toggle.toggleName)
            ?.let { inAppFlag -> inAppFlag.featureValue = newValue.toBoolean() }
            ?: debugRemoteConfigValuesSource.changeFeatureToggle(toggle.toggleName, newValue)
        load()
    }

    private fun buildFeatureToggleRows(): List<FeatureToggleRow> {
        return remoteFeatureToggles.map {
            FeatureToggleRow(
                toggleName = it.featureKey,
                toggleValue = it.value.toString(),
                isCheckable = it is BooleanFeatureToggle,
                canBeChanged = inAppFeatureFlags.isDebugRemoteConfigEnabled.featureValue
            )
        } + inAppFeatureFlags.allInAppFeatureFlags.map {
            FeatureToggleRow(
                toggleName = it.featureName,
                toggleValue = it.featureValue.toString(),
                isCheckable = true,
                canBeChanged = true
            )
        }
    }
}

package org.p2p.wallet.debug.featuretoggles

import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.remote_config.LocalFirebaseRemoteConfig
import org.p2p.wallet.common.feature_toggles.toggles.inapp.DebugTogglesFeatureFlag
import org.p2p.wallet.common.feature_toggles.toggles.inapp.InAppFeatureFlag
import org.p2p.wallet.common.feature_toggles.toggles.remote.BooleanFeatureToggle
import org.p2p.wallet.common.feature_toggles.toggles.remote.RemoteFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter

class FeatureTogglesPresenter(
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val debugRemoteConfigValuesSource: LocalFirebaseRemoteConfig,
    private val remoteFeatureToggles: List<RemoteFeatureToggle<*>>,
) : BasePresenter<FeatureTogglesContract.View>(), FeatureTogglesContract.Presenter {

    override fun load() {
        view?.showFeatureToggles(
            debugToggle = inAppFeatureFlags.isDebugRemoteConfigEnabled,
            toggleRows = buildFeatureToggleRows()
        )
    }

    override fun onToggleChanged(toggle: FeatureToggleRowItem, newValue: String) {
        val featureFlagToChange = inAppFeatureFlags.findFeatureFlagByName(toggle.toggleName)
        if (featureFlagToChange != null) {
            featureFlagToChange.featureValue = newValue.toBoolean()
        } else {
            debugRemoteConfigValuesSource.changeFeatureToggle(toggle.toggleName, newValue)
        }
        load()
    }

    override fun switchDebugRemoteConfig(isDebugEnabled: Boolean) {
        inAppFeatureFlags.isDebugRemoteConfigEnabled.featureValue = isDebugEnabled
        load()
    }

    private fun buildFeatureToggleRows(): List<FeatureToggleRowItem> =
        remoteFeatureToggles.map {
            FeatureToggleRowItem(
                toggleName = it.featureKey,
                toggleValue = it.value.toString(),
                isBooleanToggle = it is BooleanFeatureToggle,
                canBeChanged = inAppFeatureFlags.isDebugRemoteConfigEnabled.featureValue,
                isInAppFlag = false
            )
        } + inAppFeatureFlags.allInAppFeatureFlags
            .filter { it !is DebugTogglesFeatureFlag && it !is InAppFeatureFlag.InAppFeatureFlagString }
            .map {
                FeatureToggleRowItem(
                    toggleName = it.featureName,
                    toggleValue = it.featureValue.toString(),
                    isBooleanToggle = true,
                    canBeChanged = true,
                    isInAppFlag = true
                )
            }
}

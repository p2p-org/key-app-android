package org.p2p.wallet.debug.feature_toggles

import org.p2p.wallet.common.AppFeatureFlags
import org.p2p.wallet.common.feature_toggles.BooleanFeatureToggle
import org.p2p.wallet.common.feature_toggles.FeatureToggle
import org.p2p.wallet.common.feature_toggles.remote_config.LocalFirebaseRemoteConfig
import org.p2p.wallet.common.mvp.BasePresenter

class FeatureTogglesPresenter(
    private val appFeatureFlags: AppFeatureFlags,
    private val debugRemoteConfigValuesSource: LocalFirebaseRemoteConfig,
    private val featureToggles: List<FeatureToggle<*>>,
) : BasePresenter<FeatureTogglesContract.View>(), FeatureTogglesContract.Presenter {

    override fun loadFeatureToggles() {
        view?.showFeatureToggles(getFeatureToggles())
    }

    override fun onToggleChanged(toggle: FeatureToggleRow, newValue: String) {
        when (toggle.toggleName) {
            "is_polling_enabled" -> appFeatureFlags.isPollingEnabled = newValue.toBoolean()
            "is_dev_net_enabled" -> appFeatureFlags.isDevnetEnabled = newValue.toBoolean()
            "is_coin_gecko_enabled" -> appFeatureFlags.useCoinGeckoForPrices = newValue.toBoolean()
            "is_debug_remote_config_enabled" -> appFeatureFlags.isDebugRemoteConfigEnabled = newValue.toBoolean()
            else -> debugRemoteConfigValuesSource.changeFeatureToggle(toggle.toggleName, newValue)
        }

        loadFeatureToggles()
    }

    private fun getFeatureToggles(): List<FeatureToggleRow> {
        return featureToggles.map {
            FeatureToggleRow(
                toggleName = it.toggleKey,
                toggleDescription = it.toggleDescription,
                toggleValue = it.value.toString(),
                isCheckable = it is BooleanFeatureToggle,
                canBeChanged = appFeatureFlags.isDebugRemoteConfigEnabled
            )
        } + listOf(
            FeatureToggleRow(
                toggleName = "is_debug_remote_config_enabled",
                toggleDescription = "Enable local debug remote config",
                toggleValue = appFeatureFlags.isDebugRemoteConfigEnabled.toString(),
                isCheckable = true,
                canBeChanged = true
            ),
            FeatureToggleRow(
                toggleName = "is_polling_enabled",
                toggleDescription = "Enable auto update",
                toggleValue = appFeatureFlags.isPollingEnabled.toString(),
                isCheckable = true,
                canBeChanged = true
            ),
            FeatureToggleRow(
                toggleName = "is_dev_net_enabled",
                toggleDescription = "Enable DevNet env",
                toggleValue = appFeatureFlags.isDevnetEnabled.toString(),
                isCheckable = true,
                canBeChanged = true
            ),
            FeatureToggleRow(
                toggleName = "is_coin_gecko_enabled",
                toggleDescription = "Enable CoinGecko as price source",
                toggleValue = appFeatureFlags.useCoinGeckoForPrices.toString(),
                isCheckable = true,
                canBeChanged = true
            )
        )
    }
}

package org.p2p.wallet.debug.featuretoggles

import androidx.annotation.IdRes
import org.p2p.wallet.R
import org.p2p.wallet.common.AppFeatureFlags
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.settings.model.SettingsRow

class FeatureTogglesPresenter(
    private val appFeatureFlags: AppFeatureFlags
) : BasePresenter<FeatureTogglesContract.View>(), FeatureTogglesContract.Presenter {

    override fun loadFeatureToggles() {
        view?.showFeatureToggles(getFeatureToggles())
    }

    override fun onToggleCheckedListener(@IdRes toggleId: Int, toggleChecked: Boolean) {
        when (toggleId) {
            R.id.enable_dev_net -> appFeatureFlags.isDevnetEnabled = toggleChecked
            R.id.polling_enabled -> appFeatureFlags.isPollingEnabled = toggleChecked
            R.id.coin_gecko_enabled -> appFeatureFlags.useCoinGeckoForPrices = toggleChecked
        }
    }

    private fun getFeatureToggles(): List<SettingsRow.Toggle> {
        return listOf(
            SettingsRow.Toggle(
                titleResId = R.string.feature_auto_update,
                toggleId = R.id.polling_enabled,
                toggleChecked = appFeatureFlags.isPollingEnabled
            ),
            SettingsRow.Toggle(
                titleResId = R.string.feature_dev_net,
                toggleId = R.id.enable_dev_net,
                toggleChecked = appFeatureFlags.isDevnetEnabled
            ),
            SettingsRow.Toggle(
                titleResId = R.string.feature_coin_gecko,
                toggleId = R.id.coin_gecko_enabled,
                toggleChecked = appFeatureFlags.useCoinGeckoForPrices
            ),
        )
    }
}

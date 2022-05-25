package org.p2p.wallet.debug.featuretoggles

import androidx.annotation.IdRes
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.AppFeatureFlags
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.settings.model.SettingsRow

class FeatureTogglesPresenter(
    private val appFeatureFlags: AppFeatureFlags
) : BasePresenter<FeatureTogglesContract.View>(), FeatureTogglesContract.Presenter {

    override fun loadData() {
        launch {
            view?.showSettings(getSettingsRows())
        }
    }

    override fun onToggleCheckedListener(@IdRes toggleId: Int, toggleChecked: Boolean) {
        when (toggleId) {
            R.id.enable_dev_net -> appFeatureFlags.setIsDevnetEnabled(toggleChecked)
            R.id.polling_enabled -> appFeatureFlags.setPollingEnabled(toggleChecked)
        }
    }

    private fun getSettingsRows(): List<SettingsRow> {
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
        )
    }
}

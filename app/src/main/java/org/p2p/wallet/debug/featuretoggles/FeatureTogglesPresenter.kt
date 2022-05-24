package org.p2p.wallet.debug.featuretoggles

import android.content.res.Resources
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.settings.model.SettingsRow

class FeatureTogglesPresenter(
    private val resources: Resources,
) : BasePresenter<FeatureTogglesContract.View>(), FeatureTogglesContract.Presenter {

    override fun loadData() {
        launch {
            view?.showSettings(getToggles())
        }
    }

    private fun getToggles(): List<SettingsRow> {
        return listOf(
            SettingsRow.Section(
                titleResId = R.string.debug_settings_deeplinks_title,
                iconRes = R.drawable.ic_network
            ),
            SettingsRow.Section(
                titleResId = R.string.debug_settings_feature_toggles_title,
                iconRes = R.drawable.ic_home_settings
            ),
            SettingsRow.Section(
                titleResId = R.string.debug_settings_logs_title,
                subtitle = resources.getString(R.string.debug_settings_logs_subtitle),
                iconRes = R.drawable.ic_settings_cloud
            )
        )
    }
}

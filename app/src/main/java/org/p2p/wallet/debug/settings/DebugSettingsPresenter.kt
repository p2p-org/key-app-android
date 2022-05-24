package org.p2p.wallet.debug.settings

import android.content.res.Resources
import kotlinx.coroutines.launch
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.settings.model.SettingsRow

class DebugSettingsPresenter(
    environmentManager: EnvironmentManager,
    private val resources: Resources,
) : BasePresenter<DebugSettingsContract.View>(), DebugSettingsContract.Presenter {

    private var networkName = environmentManager.loadEnvironment().name

    override fun loadData() {
        launch {
            val settings = getMainSettings() +
                getAppInfoSettings()
            view?.showSettings(settings)
        }
    }

    override fun onNetworkChanged(newName: String) {
        this.networkName = newName
        loadData()
    }

    private fun getMainSettings(): List<SettingsRow> {
        return listOf(
            SettingsRow.Section(
                titleResId = R.string.debug_settings_deeplinks_title,
                iconRes = R.drawable.ic_network
            ),
            SettingsRow.Section(
                titleResId = R.string.settings_network,
                subtitle = networkName,
                iconRes = R.drawable.ic_settings_network
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

    private fun getAppInfoSettings(): List<SettingsRow> {
        return listOf(
            SettingsRow.Title(R.string.debug_settings_app_info, isDivider = true),
            SettingsRow.Section(
                titleResId = R.string.settings_app_version,
                subtitle = "${BuildConfig.BUILD_TYPE}-${BuildConfig.VERSION_NAME}",
                iconRes = R.drawable.ic_settings_app_version
            )
        )
    }
}

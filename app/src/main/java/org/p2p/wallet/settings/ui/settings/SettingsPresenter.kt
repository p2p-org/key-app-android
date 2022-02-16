package org.p2p.wallet.settings.ui.settings

import android.content.Context
import kotlinx.coroutines.launch
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.auth.analytics.AdminAnalytics
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.settings.model.SettingsRow

class SettingsPresenter(
    private val usernameInteractor: UsernameInteractor,
    private val authInteractor: AuthInteractor,
    private val environmentManager: EnvironmentManager,
    private val adminAnalytics: AdminAnalytics
    private val appRestarter: AppRestarter,
    private val context: Context
) : BasePresenter<SettingsContract.View>(), SettingsContract.Presenter {

    var networkName = environmentManager.loadEnvironment().name

    override fun loadData() {
        launch {
            val username = usernameInteractor.getUsername()?.getFullUsername(context).orEmpty()
            val settings =
                getProfileSettings(username) + getNetworkSettings() + getAppearanceSettings()
            view?.showSettings(settings)
        }
    }

    override fun logout() {
        launch {
            authInteractor.logout()
            appRestarter.restartApp()
        }
    }

    override fun onUsernameClicked() {
        if (usernameInteractor.usernameExists()) {
            view?.showUsername()
        } else {
            view?.showReserveUsername()
        }
    }

    override fun onNetworkChanged(newName: String) {
        this.networkName = newName
        loadData()
    }

    private fun getProfileSettings(username: String): List<SettingsRow> {
        return listOf(
            SettingsRow.Title(R.string.settings_profile),
            SettingsRow.Section(
                titleResId = R.string.settings_username,
                subtitle = username,
                iconRes = R.drawable.ic_settings_user
            ),
            SettingsRow.Logout()
        )
    }

    private fun getNetworkSettings(): List<SettingsRow> {
        return listOf(
            SettingsRow.Title(R.string.settings_security_and_network, isDivider = true),
            SettingsRow.Section(
                titleResId = R.string.settings_wallet_pin,
                subtitleRes = R.string.settings_wallet_pin_subtitle,
                iconRes = R.drawable.ic_settings_pin
            ),
            SettingsRow.Section(
                titleResId = R.string.settings_app_security,
                subtitleRes = R.string.settings_app_security_subtitle,
                iconRes = R.drawable.ic_settings_security
            ),
            SettingsRow.Section(
                titleResId = R.string.settings_network,
                subtitle = networkName,
                iconRes = R.drawable.ic_settings_network
            )
        )
    }

    private fun getAppearanceSettings(): List<SettingsRow> {
        return listOf(
            SettingsRow.Title(R.string.settings_appearance, isDivider = true),
            SettingsRow.Section(
                titleResId = R.string.settings_zero_balances,
                subtitleRes = R.string.settings_zero_balances_subtitle,
                iconRes = R.drawable.ic_settings_eye,
                isDivider = true
            ),
            SettingsRow.Section(
                titleResId = R.string.settings_app_version,
                subtitle = BuildConfig.VERSION_NAME,
                iconRes = R.drawable.ic_settings_app_version
            )
        )
    }
}
package org.p2p.wallet.settings.ui.settings

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.AppRestarter
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.settings.interactor.SettingsInteractor

class SettingsPresenter(
    private val settingsInteractor: SettingsInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val appRestarter: AppRestarter
) : BasePresenter<SettingsContract.View>(), SettingsContract.Presenter {

    override fun loadData() {
        launch {
            val username = usernameInteractor.getUsername()?.username.orEmpty()
            val profileItems = settingsInteractor.getProfileSettings(username)
            val networkItems = settingsInteractor.getNetworkSettings()
            val appearanceItems = settingsInteractor.getAppearanceSettings()
            val settings = profileItems + networkItems + appearanceItems
            view?.showSettings(settings)
        }
    }
}
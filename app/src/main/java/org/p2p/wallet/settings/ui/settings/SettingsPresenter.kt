package org.p2p.wallet.settings.ui.settings

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.AuthInteractor
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
            profileItems.forEach { profileItem ->
                profileItem.onItemClickListener = { view?.onProfileItemClicked(it.titleRes) }
            }
            view?.showProfile(profileItems)
        }
    }
}
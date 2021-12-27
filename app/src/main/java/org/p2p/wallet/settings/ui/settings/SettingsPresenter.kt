package org.p2p.wallet.settings.ui.settings

import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.AuthInteractor
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.user.interactor.UserInteractor

class SettingsPresenter(
    private val authInteractor: AuthInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val usernameInteractor: UsernameInteractor,
    private val appScope: AppScope
) : BasePresenter<SettingsContract.View>(), SettingsContract.Presenter {

    override fun loadData() {
        val isHidden = settingsInteractor.isZerosHidden()
        view?.showHiddenBalance(isHidden)

        val username = usernameInteractor.getUsername()
        view?.showUsername(username)
    }

    override fun setZeroBalanceHidden(isHidden: Boolean) {
        settingsInteractor.setZeroBalanceHidden(isHidden)
        view?.showHiddenBalance(isHidden)
    }

    override fun logout() {
        launch {
            authInteractor.logout()
            appScope.cancel()
            view?.showAuthorization()
        }
    }

    override fun onUsernameClicked() {
        val usernameExists = usernameInteractor.usernameExists()
        if (usernameExists) {
            view?.openUsernameScreen()
        } else {
            view?.openReserveUsernameScreen()
        }
    }
}
package com.p2p.wallet.settings.ui

import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.settings.interactor.SettingsInteractor
import kotlinx.coroutines.launch

class SettingsPresenter(
    private val authInteractor: AuthInteractor,
    private val settingsInteractor: SettingsInteractor
) : BasePresenter<SettingsContract.View>(), SettingsContract.Presenter {

    override fun loadData() {
        val isHidden = settingsInteractor.isHidden()
        view?.showHiddenBalance(isHidden)
    }

    override fun setZeroBalanceHidden(isHidden: Boolean) {
        settingsInteractor.setZeroBalanceHidden(isHidden)
        view?.showHiddenBalance(isHidden)
    }

    override fun logout() {
        launch {
            authInteractor.logout()
            view?.showAuthorization()
        }
    }
}
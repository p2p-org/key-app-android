package com.p2p.wallet.settings.ui.settings

import com.p2p.wallet.auth.interactor.AuthInteractor
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.settings.interactor.SettingsInteractor
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.launch

class SettingsPresenter(
    private val authInteractor: AuthInteractor,
    private val userInteractor: UserInteractor,
    private val settingsInteractor: SettingsInteractor
) : BasePresenter<SettingsContract.View>(), SettingsContract.Presenter {

    override fun loadData() {
        val isHidden = settingsInteractor.isZerosHidden()
        view?.showHiddenBalance(isHidden)
    }

    override fun setZeroBalanceHidden(isHidden: Boolean) {
        settingsInteractor.setZeroBalanceHidden(isHidden)
        view?.showHiddenBalance(isHidden)
    }

    override fun logout() {
        launch {
            authInteractor.logout()
            userInteractor.clearMemoryData()
            view?.showAuthorization()
        }
    }
}
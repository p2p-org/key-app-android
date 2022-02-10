package org.p2p.wallet.settings.ui.zerobalances

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.settings.interactor.SettingsInteractor

class SettingsZeroBalancesPresenter(private val settingsInteractor: SettingsInteractor) :
    BasePresenter<SettingsZeroBalanceContract.View>(),
    SettingsZeroBalanceContract.Presenter {

    override fun attach(view: SettingsZeroBalanceContract.View) {
        super.attach(view)
        view.showZeroBalances(settingsInteractor.isZerosHidden())
    }

    override fun setZeroBalancesVisibility(isVisible: Boolean) {
        settingsInteractor.setZeroBalanceHidden(isVisible)
    }
}
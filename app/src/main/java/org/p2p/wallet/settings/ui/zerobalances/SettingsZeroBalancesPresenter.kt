package org.p2p.wallet.settings.ui.zerobalances

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.settings.interactor.SettingsInteractor

class SettingsZeroBalancesPresenter(private val settingsInteractor: SettingsInteractor) :
    BasePresenter<SettingsZeroBalanceContract.View>(),
    SettingsZeroBalanceContract.Presenter {

    private var isZeroBalanceHidden = settingsInteractor.isZerosHidden()
    override fun attach(view: SettingsZeroBalanceContract.View) {
        super.attach(view)
        view.showZeroBalances(!isZeroBalanceHidden)
    }

    override fun setZeroBalancesVisibility(isHidden: Boolean) {
        isZeroBalanceHidden = isHidden
    }

    override fun save() {
        settingsInteractor.setZeroBalanceHidden(isZeroBalanceHidden)
        view?.close(!isZeroBalanceHidden)
    }
}

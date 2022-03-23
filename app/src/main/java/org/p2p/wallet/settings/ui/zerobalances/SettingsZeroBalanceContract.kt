package org.p2p.wallet.settings.ui.zerobalances

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SettingsZeroBalanceContract {
    interface View : MvpView {
        fun showZeroBalances(isVisible: Boolean)
        fun close(isZeroBalanceVisible: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun setZeroBalancesVisibility(isHidden: Boolean)
        fun save()
    }
}

package org.p2p.wallet.striga.iban

import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface StrigaUserIbanDetailsContract {
    interface View : MvpView {
        fun showIbanDetails(details: List<MainCellModel>)
    }

    interface Presenter : MvpPresenter<View>
}

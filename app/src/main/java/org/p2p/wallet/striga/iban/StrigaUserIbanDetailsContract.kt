package org.p2p.wallet.striga.iban

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface StrigaUserIbanDetailsContract {
    interface View : MvpView {
        fun showIbanDetails(details: List<AnyCellItem>)
        fun navigateBack()
    }

    interface Presenter : MvpPresenter<View>
}

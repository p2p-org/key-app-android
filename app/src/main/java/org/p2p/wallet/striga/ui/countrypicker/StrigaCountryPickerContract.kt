package org.p2p.wallet.striga.ui.countrypicker

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface StrigaCountryPickerContract {

    interface View : MvpView {
        fun showCountries(items: List<AnyCellItem>)
    }

    interface Presenter : MvpPresenter<View> {
        fun search(text: String)
    }
}

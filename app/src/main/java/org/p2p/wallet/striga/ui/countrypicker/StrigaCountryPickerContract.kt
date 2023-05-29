package org.p2p.wallet.striga.ui.countrypicker

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.model.StrigaCountryPickerItem

interface StrigaCountryPickerContract {

    interface View : MvpView {
        fun showCountries(items: List<StrigaCountryPickerItem>)
    }

    interface Presenter : MvpPresenter<View>
}

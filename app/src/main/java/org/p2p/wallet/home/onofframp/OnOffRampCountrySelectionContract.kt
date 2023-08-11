package org.p2p.wallet.home.onofframp

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface OnOffRampCountrySelectionContract {

    interface View : MvpView {
        fun setCurrentCountry(country: CountryCode)
        fun showCountryPicker(selectedItem: CountryCode?)
        fun navigateNext()
    }

    interface Presenter : MvpPresenter<View> {
        fun onCurrentCountryChanged(selectedCountry: CountryCode)
        fun onCountryClicked()
        fun onNextClicked()
    }
}

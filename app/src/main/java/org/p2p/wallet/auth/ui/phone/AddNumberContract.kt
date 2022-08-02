package org.p2p.wallet.auth.ui.phone

import org.p2p.wallet.auth.ui.phone.model.CountryCode
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface AddNumberContract {
    interface View : MvpView {
        fun showDefaultCountryCode(country: CountryCode?)
        fun update(countryCode: CountryCode)
        fun showNoCountry()
        fun onNewCountryDetected(countryCode: CountryCode)
        fun showCountryPicked(selectedCountryCode: CountryCode?)
        fun showEnabled(isEnabled: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun onCountryCodeChanged(newCountryCode: String)
        fun onPhoneChanged(phoneNumber: String)
        fun onCountryChanged(newCountry: CountryCode)
        fun onCountryClicked()
    }
}

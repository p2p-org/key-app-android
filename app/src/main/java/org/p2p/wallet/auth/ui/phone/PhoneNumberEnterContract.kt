package org.p2p.wallet.auth.ui.phone

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface PhoneNumberEnterContract {
    interface View : MvpView {
        enum class ContinueButtonState {
            ENABLED_TO_CONTINUE,
            DISABLED_INPUT_IS_EMPTY
        }

        fun showDefaultCountryCode(country: CountryCode?)
        fun update(countryCode: CountryCode?)
        fun onNewCountryDetected(countryCode: CountryCode)
        fun showCountryCodePicker(selectedCountryCode: CountryCode?)
        fun navigateToSmsInput()
        fun setContinueButtonState(state: ContinueButtonState)
        fun showSmsDeliveryFailedForNumber()
        fun navigateToAccountBlocked()
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun onCountryCodeChanged(newCountryCode: String)
        fun onPhoneChanged(phoneNumber: String)
        fun onCountryCodeChanged(newCountry: CountryCode)
        fun onCountryCodeInputClicked()
        fun submitUserPhoneNumber(phoneNumber: String)
    }
}

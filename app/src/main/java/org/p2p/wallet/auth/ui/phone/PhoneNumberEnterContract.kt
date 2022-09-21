package org.p2p.wallet.auth.ui.phone

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.ui.generalerror.GeneralErrorScreenError
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface PhoneNumberEnterContract {
    interface View : MvpView {
        fun initCreateWalletViews()
        fun initRestoreWalletViews()

        fun showDefaultCountryCode(defaultCountryCode: CountryCode?)
        fun update(countryCode: CountryCode?)
        fun onNewCountryDetected(countryCode: CountryCode)
        fun showCountryCodePicker(selectedCountryCode: CountryCode?)
        fun navigateToSmsInput()
        fun setContinueButtonState(state: PhoneNumberScreenContinueButtonState)
        fun showSmsDeliveryFailedForNumber()
        fun navigateToAccountBlocked()
        fun navigateToCriticalErrorScreen(error: GeneralErrorScreenError)
    }

    interface Presenter : MvpPresenter<View> {
        fun onCountryCodeChanged(newCountryCode: String)
        fun onPhoneChanged(phoneNumber: String)
        fun onCountryCodeChanged(newCountry: CountryCode)
        fun onCountryCodeInputClicked()
        fun submitUserPhoneNumber(phoneNumber: String)
    }
}

package org.p2p.wallet.striga.signup

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

interface StrigaSignUpFirstStepContract {
    interface View : MvpView {
        fun updateSignupField(type: StrigaSignupDataType, newValue: String)
        fun navigateNext()
        fun setErrors(errors: List<StrigaSignupFieldState>)
        fun clearErrors()
        fun clearError(type: StrigaSignupDataType)
        fun setButtonIsEnabled(isEnabled: Boolean)
        fun scrollToFirstError(type: StrigaSignupDataType)
        fun showCountryPicker()
        fun setupPhoneCountryCodePicker(selectedCountryCode: CountryCode?, selectedPhoneNumber: String?)
        fun showCountryCode(countryCode: CountryCode?)
        fun onNewCountryDetected(countryCode: CountryCode)
        fun showCountryCodePicker(selectedCountryCode: CountryCode?)
    }

    interface Presenter : MvpPresenter<View> {
        fun onFieldChanged(newValue: String, type: StrigaSignupDataType)
        fun onCountryChanged(newCountry: Country)
        fun onCountryClicked()
        fun saveChanges()
        fun onSubmit()
        fun onCountryCodeChanged(newCountryCode: String)
        fun onCountryCodeChanged(newCountry: CountryCode?)
        fun onPhoneNumberChanged(newPhone: String)
        fun onCountryCodeInputClicked()
    }
}

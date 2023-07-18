package org.p2p.wallet.striga.signup.steps.first

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupFieldState
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
        fun showCountryOfBirthPicker(countryCode: CountryCode?)
        fun setupPhoneCountryCodePicker(selectedCountryCode: CountryCode?, selectedPhoneNumber: String?)
        fun showPhoneCountryCode(countryCode: CountryCode?)
        fun showPhoneCountryCodePicker(selectedCountryCode: CountryCode?)
    }

    interface Presenter : MvpPresenter<View> {
        fun onFieldChanged(type: StrigaSignupDataType, newValue: String)
        fun onCountryOfBirthdayChanged(newCountry: CountryCode)
        fun onCountryOfBirthClicked()
        fun saveChanges()
        fun onSubmit()
        fun onPhoneCountryCodeChanged(newCountry: CountryCode, changedByUser: Boolean)
        fun onPhoneNumberChanged(newPhone: String)
        fun onPhoneCountryCodeClicked()
    }
}

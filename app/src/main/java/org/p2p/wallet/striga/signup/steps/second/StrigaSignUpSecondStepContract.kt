package org.p2p.wallet.striga.signup.steps.second

import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaOccupation
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.presetpicker.interactor.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

interface StrigaSignUpSecondStepContract {
    interface View : MvpView {
        fun updateSignupField(type: StrigaSignupDataType, newValue: String)
        fun navigateNext()
        fun navigateToPhoneError()
        fun setErrors(errors: List<StrigaSignupFieldState>)
        fun clearErrors()
        fun clearError(type: StrigaSignupDataType)
        fun setButtonIsEnabled(isEnabled: Boolean)
        fun setProgressIsVisible(isVisible: Boolean)
        fun scrollToFirstError(type: StrigaSignupDataType)
        fun showSourceOfFundsPicker(selectedItem: StrigaSourceOfFunds?)
        fun showOccupationPicker(selectedItem: StrigaOccupation?)
        fun showCurrentCountryPicker(selectedItem: CountryCode?)
    }

    interface Presenter : MvpPresenter<View> {
        fun onFieldChanged(type: StrigaSignupDataType, newValue: String)
        fun onSubmit()
        fun saveChanges()
        fun onPresetDataChanged(selectedItem: StrigaPresetDataItem)
        fun onOccupationClicked()
        fun onFundsClicked()
        fun onCountryClicked()
    }
}

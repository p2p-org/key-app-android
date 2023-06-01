package org.p2p.wallet.striga.signup

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

interface StrigaSignUpSecondStepContract {
    interface View : MvpView {
        fun updateSignupField(type: StrigaSignupDataType, newValue: String)
        fun navigateNext()
        fun setErrors(errors: List<StrigaSignupFieldState>)
        fun clearErrors()
        fun setButtonIsEnabled(isEnabled: Boolean)
        fun scrollToFirstError(type: StrigaSignupDataType)
        fun showOccupationPicker(selectedValue: StrigaOccupation?)
        fun showFundsPicker(selectedValue: StrigaSourceOfFunds?)
    }

    interface Presenter : MvpPresenter<View> {
        fun onFieldChanged(newValue: String, type: StrigaSignupDataType)
        fun onSourceOfFundsChanged(newValue: StrigaSourceOfFunds)
        fun onOccupationChanged(newValue: StrigaOccupation)
        fun onSourceOfFundsClicked()
        fun onOccupationClicked()
        fun onSubmit()
        fun saveChanges()
    }
}

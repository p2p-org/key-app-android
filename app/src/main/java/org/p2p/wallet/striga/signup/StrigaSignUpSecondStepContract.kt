package org.p2p.wallet.striga.signup

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

interface StrigaSignUpSecondStepContract {
    interface View : MvpView {
        fun updateSignupField(type: StrigaSignupDataType, newValue: String)
        fun navigateNext()
        fun setErrors(errors: List<StrigaSignupFieldState>)
        fun clearErrors()
        fun clearError(type: StrigaSignupDataType)
        fun setButtonIsEnabled(isEnabled: Boolean)
        fun setProgressIsVisible(visible: Boolean)
        fun scrollToFirstError(type: StrigaSignupDataType)
    }

    interface Presenter : MvpPresenter<View> {
        fun onFieldChanged(newValue: String, type: StrigaSignupDataType)
        fun onSubmit()
        fun saveChanges()
        fun onPresetDataChanged(selectedItem: StrigaPresetDataItem)
    }
}

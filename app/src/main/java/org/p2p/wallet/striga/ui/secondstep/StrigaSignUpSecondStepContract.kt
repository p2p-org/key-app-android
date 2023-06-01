package org.p2p.wallet.striga.ui.secondstep

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

interface StrigaSignUpSecondStepContract {
    interface View : MvpView {
        fun updateSignupField(newValue: String, type: StrigaSignupDataType)
        fun showOccupationPicker(selectedValue: StrigaOccupation?)
        fun showFundsPicker(selectedValue: StrigaSourceOfFunds?)
    }

    interface Presenter : MvpPresenter<View> {
        fun onFieldChanged(newValue: String, type: StrigaSignupDataType)
        fun onSourceOfFundsChanged(newValue: StrigaSourceOfFunds)
        fun onOccupationChanged(newValue: StrigaOccupation)
        fun onSourceOfFundsClicked()
        fun onOccupationClicked()
    }
}

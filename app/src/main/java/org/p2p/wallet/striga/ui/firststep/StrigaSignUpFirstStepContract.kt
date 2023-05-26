package org.p2p.wallet.striga.ui.firststep

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.model.StrigaSignupDataType

interface StrigaSignUpFirstStepContract {
    interface View : MvpView {
        fun updateSignupField(newValue: String, type: StrigaSignupDataType)
    }
    interface Presenter : MvpPresenter<View> {
        fun onFieldChanged(newValue: String, type: StrigaSignupDataType)
    }
}

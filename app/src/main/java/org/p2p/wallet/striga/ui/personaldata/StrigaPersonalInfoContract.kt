package org.p2p.wallet.striga.ui.personaldata

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.model.StrigaSignupDataType

interface StrigaPersonalInfoContract {
    interface View : MvpView {
        fun updateText(newValue: String, type: StrigaSignupDataType)
    }
    interface Presenter : MvpPresenter<View> {
        fun onTextChanged(newValue: String, type: StrigaSignupDataType)
    }
}

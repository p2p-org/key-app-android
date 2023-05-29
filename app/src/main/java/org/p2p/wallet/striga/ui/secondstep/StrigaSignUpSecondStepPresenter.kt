package org.p2p.wallet.striga.ui.secondstep

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignUpSecondStepPresenter :
    BasePresenter<StrigaSignUpSecondStepContract.View>(),
    StrigaSignUpSecondStepContract.Presenter {

    override fun onFieldChanged(newValue: String, type: StrigaSignupDataType) = Unit
}

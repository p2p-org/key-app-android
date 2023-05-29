package org.p2p.wallet.striga.ui.firststep

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignUpFirstStepPresenter :
    BasePresenter<StrigaSignUpFirstStepContract.View>(),
    StrigaSignUpFirstStepContract.Presenter {

    override fun onFieldChanged(newValue: String, tag: StrigaSignupDataType) = Unit
}

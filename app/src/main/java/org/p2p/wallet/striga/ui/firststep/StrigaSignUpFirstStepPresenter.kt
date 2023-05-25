package org.p2p.wallet.striga.ui.firststep

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.model.StrigaSignupDataType

class StrigaSignUpFirstStepPresenter :
    BasePresenter<StrigaSignUpFirstStepContract.View>(),
    StrigaSignUpFirstStepContract.Presenter {

    override fun onTextChanged(newValue: String, tag: StrigaSignupDataType) = Unit
}

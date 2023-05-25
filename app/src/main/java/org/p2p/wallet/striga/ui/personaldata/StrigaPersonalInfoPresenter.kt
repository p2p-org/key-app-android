package org.p2p.wallet.striga.ui.personaldata

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.model.StrigaSignupDataType

class StrigaPersonalInfoPresenter :
    BasePresenter<StrigaPersonalInfoContract.View>(),
    StrigaPersonalInfoContract.Presenter {

    override fun onTextChanged(newValue: String, tag: StrigaSignupDataType) = Unit
}

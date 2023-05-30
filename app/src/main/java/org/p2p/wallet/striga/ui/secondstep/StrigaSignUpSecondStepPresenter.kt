package org.p2p.wallet.striga.ui.secondstep

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.ui.countrypicker.StrigaItemCellMapper

class StrigaSignUpSecondStepPresenter(
    private val strigaItemCellMapper: StrigaItemCellMapper
) :
    BasePresenter<StrigaSignUpSecondStepContract.View>(),
    StrigaSignUpSecondStepContract.Presenter {

    private var selectedSourceOfFunds: StrigaSourceOfFunds? = null
    private var selectedOccupation: StrigaOccupation? = null

    override fun onFieldChanged(newValue: String, type: StrigaSignupDataType) = Unit

    override fun onSourceOfFundsChanged(newValue: StrigaSourceOfFunds) {
        selectedSourceOfFunds = newValue
        view?.updateSignupField(
            newValue = strigaItemCellMapper.toUiTitle(newValue.sourceName),
            type = StrigaSignupDataType.SOURCE_OF_FUNDS
        )
    }

    override fun onOccupationChanged(newValue: StrigaOccupation) {
        selectedOccupation = newValue
        view?.updateSignupField(
            newValue = strigaItemCellMapper.toUiTitle(newValue.occupationName),
            type = StrigaSignupDataType.OCCUPATION
        )
    }

    override fun onSourceOfFundsClicked() {
        view?.showFundsPicker(selectedSourceOfFunds)
    }

    override fun onOccupationClicked() {
        view?.showOccupationPicker(selectedOccupation)
    }
}

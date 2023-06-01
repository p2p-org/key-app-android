package org.p2p.wallet.striga.signup.ui

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.signup.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.countrypicker.StrigaItemCellMapper
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData

class StrigaSignUpSecondStepPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaSignupInteractor,
    private val strigaItemCellMapper: StrigaItemCellMapper
) :
    BasePresenter<StrigaSignUpSecondStepContract.View>(dispatchers.ui),
    StrigaSignUpSecondStepContract.Presenter {

    private val signupData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()
    private var selectedSourceOfFunds: StrigaSourceOfFunds? = null
    private var selectedOccupation: StrigaOccupation? = null

    override fun firstAttach() {
        super.firstAttach()
        launch {
            initialLoadSignupData()
        }
    }

    override fun onFieldChanged(newValue: String, type: StrigaSignupDataType) {
        setData(type, newValue)
        // enabling button if something changed
        view?.setButtonIsEnabled(true)
    }

    override fun onSourceOfFundsChanged(newValue: StrigaSourceOfFunds) {
        selectedSourceOfFunds = newValue
        view?.updateSignupField(
            newValue = strigaItemCellMapper.toUiTitle(newValue.sourceName),
            type = StrigaSignupDataType.SOURCE_OF_FUNDS
        )
        setData(StrigaSignupDataType.SOURCE_OF_FUNDS, newValue.sourceName)
    }

    override fun onOccupationChanged(newValue: StrigaOccupation) {
        selectedOccupation = newValue
        view?.updateSignupField(
            newValue = strigaItemCellMapper.toUiTitle(newValue.occupationName),
            type = StrigaSignupDataType.OCCUPATION
        )
        setData(StrigaSignupDataType.OCCUPATION, newValue.occupationName)
    }

    override fun onSourceOfFundsClicked() {
        view?.showFundsPicker(selectedSourceOfFunds)
    }

    override fun onOccupationClicked() {
        view?.showOccupationPicker(selectedOccupation)
    }

    override fun onSubmit() {
        view?.clearErrors()

        val (isValid, states) = interactor.validateSecondStep(signupData)

        if (isValid) {
            view?.navigateNext()
        } else {
            view?.setErrors(states)
            // disable button is there are errors
            view?.setButtonIsEnabled(false)
            states.firstOrNull { !it.isValid }?.let {
                view?.scrollToFirstError(it.type)
            }
        }
    }

    override fun saveChanges() {
        interactor.saveChanges(signupData.values)
    }

    private suspend fun initialLoadSignupData() {
        val data = interactor.getSignupData()
        data.forEach {
            setData(it.type, it.value.orEmpty())
            view?.updateSignupField(it.type, it.value.orEmpty())
        }
    }

    private fun setData(type: StrigaSignupDataType, newValue: String) {
        signupData[type] = StrigaSignupData(type = type, value = newValue)
    }
}

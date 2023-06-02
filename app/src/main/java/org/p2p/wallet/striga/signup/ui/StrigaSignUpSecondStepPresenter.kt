package org.p2p.wallet.striga.signup.ui

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.presetpicker.mapper.StrigaItemCellMapper
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.signup.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignUpSecondStepPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaSignupInteractor,
    private val onboardingInteractor: StrigaOnboardingInteractor,
    private val strigaItemCellMapper: StrigaItemCellMapper
) : BasePresenter<StrigaSignUpSecondStepContract.View>(dispatchers.ui),
    StrigaSignUpSecondStepContract.Presenter {

    private val cachedSignupData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()

    override fun attach(view: StrigaSignUpSecondStepContract.View) {
        super.attach(view)
        launch {
            initialLoadSignupData()
        }
    }

    override fun onFieldChanged(newValue: String, type: StrigaSignupDataType) {
        cachedSignupData[type] = StrigaSignupData(type = type, value = newValue)
        // enabling button if something changed
        view?.setButtonIsEnabled(true)
    }

    private fun onSourceOfFundsChanged(newValue: StrigaSourceOfFunds) {
        view?.updateSignupField(
            newValue = strigaItemCellMapper.toUiTitle(newValue.sourceName),
            type = StrigaSignupDataType.SOURCE_OF_FUNDS
        )
    }

    private fun onOccupationChanged(newValue: StrigaOccupation) {
        view?.updateSignupField(
            newValue = strigaItemCellMapper.toUiTitle(newValue.occupationName),
            type = StrigaSignupDataType.OCCUPATION
        )
    }

    private fun onCountryChanged(newValue: Country) {
        view?.updateSignupField(
            newValue = "${newValue.flagEmoji} ${newValue.name}",
            type = StrigaSignupDataType.COUNTRY
        )
    }

    override fun onSubmit() {
        view?.clearErrors()

        val (isValid, states) = interactor.validateSecondStep(cachedSignupData)

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
        interactor.saveChanges(cachedSignupData.values)
    }

    private suspend fun initialLoadSignupData() {
        val data = interactor.getSignupDataSecondStep()
        data.forEach { savedSignupData ->
            cachedSignupData[savedSignupData.type] = StrigaSignupData(
                type = savedSignupData.type,
                value = savedSignupData.value.orEmpty()
            )
            view?.updateSignupField(savedSignupData.type, savedSignupData.value.orEmpty())
        }

        cachedSignupData[StrigaSignupDataType.OCCUPATION]?.value?.let {
            onboardingInteractor.getOccupationByName(it)
                ?.also(::onOccupationChanged)
        }
        cachedSignupData[StrigaSignupDataType.SOURCE_OF_FUNDS]?.value?.let {
            onboardingInteractor.getSourcesOfFundsByName(it)
                ?.also(::onSourceOfFundsChanged)
        }
        cachedSignupData[StrigaSignupDataType.COUNTRY]?.value?.let {
            interactor.findCountryByIsoAlpha2(it)
                ?.also(::onCountryChanged)
        }
    }
}

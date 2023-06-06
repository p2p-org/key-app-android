package org.p2p.wallet.striga.signup.ui

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.presetpicker.mapper.StrigaItemCellMapper
import org.p2p.wallet.striga.signup.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor

class StrigaSignUpSecondStepPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaSignupInteractor,
    private val onboardingInteractor: StrigaOnboardingInteractor,
    private val userInteractor: StrigaUserInteractor,
    private val strigaItemCellMapper: StrigaItemCellMapper,
) :
    BasePresenter<StrigaSignUpSecondStepContract.View>(dispatchers.ui),
    StrigaSignUpSecondStepContract.Presenter {

    private val cachedSignupData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()

    override fun firstAttach() {
        super.firstAttach()
        launch {
            initialLoadSignupData()
        }
    }

    override fun onFieldChanged(newValue: String, type: StrigaSignupDataType) {
        setCachedData(type, newValue)

        view?.clearError(type)
        // enabling button if something changed
        view?.setButtonIsEnabled(true)
    }

    override fun onPresetDataChanged(selectedItem: StrigaPresetDataItem) {
        when (selectedItem) {
            is StrigaPresetDataItem.StrigaCountryItem -> onCountryChanged(selectedItem.details)
            is StrigaPresetDataItem.StrigaOccupationItem -> onOccupationChanged(selectedItem.details)
            is StrigaPresetDataItem.StrigaSourceOfFundsItem -> onSourceOfFundsChanged(selectedItem.details)
        }
    }

    private fun onSourceOfFundsChanged(newValue: StrigaSourceOfFunds) {
        setCachedData(StrigaSignupDataType.SOURCE_OF_FUNDS, newValue.sourceName)
        view?.updateSignupField(
            newValue = strigaItemCellMapper.toUiTitle(newValue.sourceName),
            type = StrigaSignupDataType.SOURCE_OF_FUNDS
        )
    }

    private fun onOccupationChanged(newValue: StrigaOccupation) {
        setCachedData(StrigaSignupDataType.OCCUPATION, newValue.occupationName)
        view?.updateSignupField(
            newValue = strigaItemCellMapper.toUiTitle(newValue.occupationName),
            type = StrigaSignupDataType.OCCUPATION
        )
    }

    private fun onCountryChanged(newValue: Country) {
        setCachedData(StrigaSignupDataType.COUNTRY, newValue.codeAlpha2)
        view?.updateSignupField(
            newValue = "${newValue.flagEmoji} ${newValue.name}",
            type = StrigaSignupDataType.COUNTRY
        )
    }

    override fun onSubmit() {
        view?.clearErrors()

        val (isValid, states) = interactor.validateSecondStep(cachedSignupData)

        if (isValid) {
            view?.setProgressIsVisible(true)
            launch {
                try {
                    interactor.createUser()
                    userInteractor.resendSmsForVerifyPhoneNumber().unwrap()
                    view?.navigateNext()
                } catch (e: Throwable) {
                    Timber.e(e, "Unable to create striga user")
                    view?.setProgressIsVisible(false)
                    view?.showUiKitSnackBar(e.message, R.string.error_general_message)
                }
            }
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
        data.forEach { (type, value) ->
            setCachedData(type, value.orEmpty())
            view?.updateSignupField(type, value.orEmpty())
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

    private fun setCachedData(type: StrigaSignupDataType, value: String) {
        cachedSignupData[type] = StrigaSignupData(type, value)
    }
}

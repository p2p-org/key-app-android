package org.p2p.wallet.striga.signup.ui

import timber.log.Timber
import kotlin.jvm.Throws
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.signup.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.countrypicker.StrigaItemCellMapper
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.utils.DateTimeUtils

class StrigaSignUpSecondStepPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaSignupInteractor,
    private val onboardingInteractor: StrigaOnboardingInteractor,
    private val userInteractor: StrigaUserInteractor,
    private val metadataInteractor: MetadataInteractor,
    private val strigaItemCellMapper: StrigaItemCellMapper,
) :
    BasePresenter<StrigaSignUpSecondStepContract.View>(dispatchers.ui),
    StrigaSignUpSecondStepContract.Presenter {

    private val signupData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()
    private var selectedSourceOfFunds: StrigaSourceOfFunds? = null
    private var selectedOccupation: StrigaOccupation? = null
    private var selectedCountry: Country? = null

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

    override fun onCountryClicked() {
        view?.showCountryPicker(selectedCountry)
    }

    override fun onCountryChanged(newValue: Country) {
        selectedCountry = newValue
        view?.updateSignupField(
            newValue = "${newValue.flagEmoji} ${newValue.name}",
            type = StrigaSignupDataType.COUNTRY
        )
    }

    override fun onSubmit() {
        view?.clearErrors()

        val (isValid, states) = interactor.validateSecondStep(signupData)

        if (isValid) {
            view?.setProgressIsVisible(true)
            launch {
                try {
                    createUserAndSaveMetadata()
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
        mapDataForStorage()
        interactor.saveChanges(signupData.values)
    }

    private suspend fun initialLoadSignupData() {
        val data = interactor.getSignupDataSecondStep()
        data.forEach {
            setData(it.type, it.value.orEmpty())
            view?.updateSignupField(it.type, it.value.orEmpty())
        }

        signupData[StrigaSignupDataType.OCCUPATION]?.value?.let {
            selectedOccupation = onboardingInteractor.getOccupationByName(it)
                .also(::onOccupationChanged)
        }
        signupData[StrigaSignupDataType.SOURCE_OF_FUNDS]?.value?.let {
            selectedSourceOfFunds = onboardingInteractor.getSourcesOfFundsByName(it)
                .also(::onSourceOfFundsChanged)
        }

        signupData[StrigaSignupDataType.COUNTRY]?.value?.let {
            selectedCountry = interactor.findCountryByIsoAlpha2(it)
                ?.also(::onCountryChanged)
        }
    }

    private fun mapDataForStorage() {
        setData(StrigaSignupDataType.OCCUPATION, selectedOccupation?.occupationName.orEmpty())
        setData(StrigaSignupDataType.SOURCE_OF_FUNDS, selectedSourceOfFunds?.sourceName.orEmpty())
        setData(StrigaSignupDataType.COUNTRY, selectedCountry?.codeAlpha2.orEmpty())
    }

    private fun setData(type: StrigaSignupDataType, newValue: String) {
        signupData[type] = StrigaSignupData(type = type, value = newValue)
    }

    @Throws(IllegalStateException::class, StrigaDataLayerError::class)
    private suspend fun createUserAndSaveMetadata() {
        // firstly, get metadata and only then create user
        val metadata = metadataInteractor.currentMetadata
            ?: error("Metadata is not fetched")

        when (val result = userInteractor.createUser(interactor.getSignupData())) {
            is StrigaDataLayerResult.Success -> {
                val strigaMetadata = if (metadata.strigaMetadata == null) {
                    GatewayOnboardingMetadata.StrigaMetadata(
                        userId = result.value.userId,
                        userIdTimestamp = DateTimeUtils.getCurrentTimestampInSeconds()
                    )
                } else {
                    metadata.strigaMetadata.copy(
                        userId = result.value.userId,
                        userIdTimestamp = DateTimeUtils.getCurrentTimestampInSeconds()
                    )
                }

                metadataInteractor.updateMetadata(metadata.copy(strigaMetadata = strigaMetadata))
            }
            is StrigaDataLayerResult.Failure -> {
                throw result.error
            }
        }
    }
}

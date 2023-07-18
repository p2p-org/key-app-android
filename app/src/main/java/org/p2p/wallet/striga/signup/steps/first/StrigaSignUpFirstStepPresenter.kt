package org.p2p.wallet.striga.signup.steps.first

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.steps.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.steps.validation.PhoneNumberInputValidator
import org.p2p.wallet.utils.emptyString

class StrigaSignUpFirstStepPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaSignupInteractor,
    private val countryRepository: CountryCodeRepository,
    private val metadataInteractor: MetadataInteractor,
) : BasePresenter<StrigaSignUpFirstStepContract.View>(dispatchers.ui),
    StrigaSignUpFirstStepContract.Presenter {

    private val signupData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()
    private var selectedCountryOfBirth: CountryCode? = null
    private var phoneCountryCode: CountryCode? = null
    private var isSubmittedFirstTime = false

    override fun firstAttach() {
        super.firstAttach()
        launch {
            initialLoadSignupData()
        }
    }

    override fun attach(view: StrigaSignUpFirstStepContract.View) {
        super.attach(view)
        launch {
            loadPhoneCountryCode()
        }
    }

    override fun detach() {
        super.detach()
        isSubmittedFirstTime = false
    }

    override fun onFieldChanged(type: StrigaSignupDataType, newValue: String) {
        Timber.i("field $type changed with new value of size: ${newValue.length}")
        if (type != StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3) {
            setCachedData(type, newValue)
        }

        Timber.i("isSubmittedFirstTime = $isSubmittedFirstTime")
        if (isSubmittedFirstTime) {
            val validationResult = interactor.validateField(type, newValue)
            Timber.i("validation result for field $type = ${validationResult.isValid}")
            if (validationResult.isValid) {
                view?.clearError(type)
            } else {
                view?.setErrors(listOf(validationResult))
            }
        }
        // enabling button if something changed
        view?.setButtonIsEnabled(isEnabled = true)
    }

    override fun onCountryOfBirthdayChanged(newCountry: CountryCode) {
        selectedCountryOfBirth = newCountry
        setCachedData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, newCountry.nameCodeAlpha3)
        view?.updateSignupField(
            newValue = "${newCountry.flagEmoji} ${newCountry.countryName}",
            type = StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3
        )
    }

    override fun onSubmit() {
        if (!isSubmittedFirstTime) {
            isSubmittedFirstTime = true
        }
        view?.clearErrors()

        val (isValid, states) = interactor.validateFirstStep(signupData)

        if (isValid) {
            view?.navigateNext()
        } else {
            Timber.d("Validation failed: $states")
            view?.setErrors(states)
            // disable button is there are errors
            view?.setButtonIsEnabled(false)
            states.firstOrNull { !it.isValid }?.let {
                view?.scrollToFirstError(it.type)
            }
        }
    }

    override fun onPhoneCountryCodeChanged(newCountry: CountryCode, changedByUser: Boolean) {
        phoneCountryCode = newCountry
        view?.showPhoneCountryCode(phoneCountryCode)

        interactor.setPhoneValidator(
            PhoneNumberInputValidator(
                regionCodeAlpha2 = newCountry.nameCodeAlpha2,
                countryCodeRepository = countryRepository
            )
        )

        if (changedByUser) {
            view?.updateSignupField(StrigaSignupDataType.PHONE_NUMBER, emptyString())
        }
    }

    override fun onPhoneNumberChanged(newPhone: String) {
        phoneCountryCode?.let {
            val phoneWithoutCountryCode = newPhone.replace(it.phoneCodeWithPlusSign, emptyString())
            onFieldChanged(StrigaSignupDataType.PHONE_NUMBER, phoneWithoutCountryCode)
        }
    }

    override fun onPhoneCountryCodeClicked() {
        view?.showPhoneCountryCodePicker(phoneCountryCode)
    }

    override fun onCountryOfBirthClicked() {
        view?.showCountryOfBirthPicker(selectedCountryOfBirth)
    }

    override fun saveChanges() {
        mapDataForStorage()
        interactor.saveChanges(signupData.values)
    }

    private suspend fun initialLoadSignupData() {
        val firstStepData = interactor.getSignupDataFirstStep()
            .associateBy(StrigaSignupData::type)
            .toMutableMap()
        metadataInteractor.currentMetadata?.let { metadata ->
            firstStepData[StrigaSignupDataType.EMAIL] = StrigaSignupData(
                type = StrigaSignupDataType.EMAIL,
                value = metadata.socialShareOwnerEmail
            )
        }

        // fill pre-saved values as-is
        firstStepData.values.forEach {
            setCachedData(it.type, it.value.orEmpty())
            view?.updateSignupField(it.type, it.value.orEmpty())
        }
        getAndUpdateCountryField(firstStepData)
    }

    private suspend fun loadPhoneCountryCode() {
        Timber.i("Setting up phone country code")
        if (phoneCountryCode != null) {
            val selectedPhoneNumber = signupData[StrigaSignupDataType.PHONE_NUMBER]?.value
            view?.setupPhoneCountryCodePicker(
                selectedCountryCode = phoneCountryCode,
                selectedPhoneNumber = selectedPhoneNumber
            )
        } else {
            try {
                val phoneNumberWithCode = interactor.retrievePhoneNumberWithCode(
                    cachedPhoneCode = signupData[StrigaSignupDataType.PHONE_CODE_WITH_PLUS]?.value,
                    cachedPhoneNumber = signupData[StrigaSignupDataType.PHONE_NUMBER]?.value
                )

                onPhoneCountryCodeChanged(phoneNumberWithCode.phoneCode, false)

                // Using the updated phoneCode and selectedPhoneNumber
                view?.setupPhoneCountryCodePicker(
                    selectedCountryCode = phoneNumberWithCode.phoneCode,
                    selectedPhoneNumber = phoneNumberWithCode.phoneNumberNational
                )
                view?.updateSignupField(StrigaSignupDataType.PHONE_NUMBER, phoneNumberWithCode.phoneNumberNational)
            } catch (e: Throwable) {
                Timber.e(e, "Loading default country code failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private fun getAndUpdateCountryField(signupData: Map<StrigaSignupDataType, StrigaSignupData>) {
        // db stores COUNTRY_OF_BIRTH as country name code ISO 3166-1 alpha-3,
        // so we need to find country by code and convert to country name
        val selectedCountryValue = signupData[StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3]?.value.orEmpty()
        interactor.findCountryByIsoAlpha3(selectedCountryValue)
            ?.also { onCountryOfBirthdayChanged(it) }

        signupData[StrigaSignupDataType.PHONE_CODE_WITH_PLUS]?.value?.let {
            val phoneCountryCode = countryRepository.findCountryCodeByPhoneCode(it)
            phoneCountryCode?.let { countryCode -> onPhoneCountryCodeChanged(countryCode, false) }
        }
    }

    private fun mapDataForStorage() {
        selectedCountryOfBirth?.nameCodeAlpha3?.let { setCachedData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, it) }
        phoneCountryCode?.let {
            setCachedData(StrigaSignupDataType.PHONE_CODE_WITH_PLUS, it.phoneCodeWithPlusSign)
        }
    }

    private fun setCachedData(type: StrigaSignupDataType, newValue: String) {
        signupData[type] = StrigaSignupData(type = type, value = newValue)
    }
}

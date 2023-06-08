package org.p2p.wallet.striga.signup.ui

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.signup.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.PhoneNumberInputValidator

class StrigaSignUpFirstStepPresenter(
    dispatchers: CoroutineDispatchers,
    private val interactor: StrigaSignupInteractor,
    private val countryRepository: CountryCodeRepository,
) : BasePresenter<StrigaSignUpFirstStepContract.View>(dispatchers.ui),
    StrigaSignUpFirstStepContract.Presenter {

    private val signupData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()
    private var countryOfBirth: CountryCode? = null
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
        if (type != StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3) {
            setCachedData(type, newValue)
        }

        if (isSubmittedFirstTime) {
            val validationResult = interactor.validateField(type, newValue)
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
        view?.updateSignupField(
            newValue = "${newCountry.flagEmoji} ${newCountry.countryName}",
            type = StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3
        )
        setCachedData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, newCountry.nameCodeAlpha3)
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

    override fun onPhoneCountryCodeChanged(newCountry: CountryCode) {
        phoneCountryCode = newCountry
        view?.showPhoneCountryCode(phoneCountryCode)

        interactor.setPhoneValidator(
            PhoneNumberInputValidator(
                regionCodeAlpha2 = newCountry.nameCodeAlpha2,
                countryCodeRepository = countryRepository
            )
        )
    }

    override fun onPhoneNumberChanged(newPhone: String) {
        phoneCountryCode?.let {
            val phoneWithoutCountryCode = newPhone.replace(it.phoneCodeWithPlusSign, "")
            onFieldChanged(StrigaSignupDataType.PHONE_NUMBER, phoneWithoutCountryCode)
        }
    }

    override fun onPhoneCountryCodeClicked() {
        view?.showPhoneCountryCodePicker(phoneCountryCode)
    }

    override fun onCountryOfBirthClicked() {
        view?.showCountryOfBirthPicker()
    }

    override fun saveChanges() {
        mapDataForStorage()
        interactor.saveChanges(signupData.values)
    }

    private suspend fun initialLoadSignupData() {
        val data = interactor.getSignupDataFirstStep().associateBy { it.type }

        // fill pre-saved values as-is
        data.values.forEach {
            setCachedData(it.type, it.value.orEmpty())
            view?.updateSignupField(it.type, it.value.orEmpty())
        }

        getAndUpdateCountryField(data)
    }

    private suspend fun loadPhoneCountryCode() {
        if (phoneCountryCode != null) {
            val selectedPhoneNumber = signupData[StrigaSignupDataType.PHONE_NUMBER]?.value
            view?.setupPhoneCountryCodePicker(
                selectedCountryCode = phoneCountryCode,
                selectedPhoneNumber = selectedPhoneNumber
            )
        } else {
            // find default country code for phone
            try {
                phoneCountryCode = countryRepository.detectCountryOrDefault().also { countryCode ->
                    val selectedPhoneNumber = signupData[StrigaSignupDataType.PHONE_NUMBER]?.value

                    onPhoneCountryCodeChanged(countryCode)
                    view?.setupPhoneCountryCodePicker(
                        selectedCountryCode = countryCode,
                        selectedPhoneNumber = selectedPhoneNumber
                    )
                }
            } catch (e: Throwable) {
                Timber.e(e, "Loading default country code failed")
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private suspend fun getAndUpdateCountryField(signupData: Map<StrigaSignupDataType, StrigaSignupData>) {
        // db stores COUNTRY_OF_BIRTH as country name code ISO 3166-1 alpha-3,
        // so we need to find country by code and convert to country name
        val selectedCountryValue = signupData[StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3]?.value.orEmpty()
        interactor.findCountryByIsoAlpha3(selectedCountryValue)
            ?.also { onCountryOfBirthdayChanged(it) }

        getCachedPhoneCodeWithoutPlus()?.let {
            val phoneCountryCode = countryRepository.findCountryCodeByPhoneCode(it)
            phoneCountryCode?.let(::onPhoneCountryCodeChanged)
        }
    }

    private fun mapDataForStorage() {
        countryOfBirth?.nameCodeAlpha3?.let { setCachedData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, it) }
        phoneCountryCode?.let {
            setCachedData(StrigaSignupDataType.PHONE_CODE_WITH_PLUS, it.phoneCodeWithPlusSign)
        }
    }

    private fun setCachedData(type: StrigaSignupDataType, newValue: String) {
        signupData[type] = StrigaSignupData(type = type, value = newValue)
    }

    private fun getCachedPhoneCodeWithoutPlus(): String? {
        return signupData[StrigaSignupDataType.PHONE_CODE_WITH_PLUS]?.value?.let {
            check(it.isNotBlank()) { "Given blank phone code from storage!" }
            if (it[0] == '+') {
                it.substring(1)
            } else {
                // i'm not sure if it's possible, but just in case leave it here
                it
            }
        }
    }
}

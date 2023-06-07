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
    private var selectedCountryCode: CountryCode? = null

    override fun firstAttach() {
        super.firstAttach()
        launch {
            initialLoadSignupData()
            initialLoadCountryCode()
        }
    }

    private suspend fun initialLoadCountryCode() {
        if (selectedCountryCode != null) {
            val selectedPhoneNumber = signupData[StrigaSignupDataType.PHONE_NUMBER]?.value
            view?.setupPhoneCountryCodePicker(
                selectedCountryCode = selectedCountryCode,
                selectedPhoneNumber = selectedPhoneNumber
            )
            return
        }
        loadDefaultCountryCode()
    }

    override fun onFieldChanged(newValue: String, type: StrigaSignupDataType) {
        setCachedData(type, newValue)

        view?.clearError(type)
        // enabling button if something changed
        view?.setButtonIsEnabled(isEnabled = true)
    }

    override fun onCountryChanged(newCountry: CountryCode) {
        setCachedData(StrigaSignupDataType.COUNTRY_OF_BIRTH, newCountry.nameCodeAlpha3)
        view?.updateSignupField(
            newValue = "${newCountry.flagEmoji} ${newCountry.countryName}",
            type = StrigaSignupDataType.COUNTRY_OF_BIRTH
        )
    }

    override fun onSubmit() {
        view?.clearErrors()

        val phoneNumber = signupData[StrigaSignupDataType.PHONE_NUMBER]?.value.orEmpty()
        val regionCode = selectedCountryCode?.nameCodeAlpha2.orEmpty()

        interactor.addValidator(
            PhoneNumberInputValidator(
                phoneNumber = phoneNumber,
                regionCode = regionCode,
                countryCodeRepository = countryRepository
            )
        )
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

    override fun onPhoneCountryCodeChanged(newCountry: CountryCode?) {
        selectedCountryCode = newCountry
        val phoneCodeWithPlus = "+${selectedCountryCode?.phoneCode}"
        setCachedData(StrigaSignupDataType.PHONE_CODE_WITH_PLUS, phoneCodeWithPlus)
        view?.showCountryCode(selectedCountryCode)
    }

    override fun onPhoneNumberChanged(newPhone: String) {
        val countryCode = "+${selectedCountryCode?.phoneCode}"
        val phoneWithoutCountryCode = newPhone.replace(countryCode, "")
        setCachedData(StrigaSignupDataType.PHONE_NUMBER, phoneWithoutCountryCode)
        view?.clearError(StrigaSignupDataType.PHONE_NUMBER)
    }

    override fun onCountryCodeInputClicked() {
        view?.showCountryCodePicker(selectedCountryCode)
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

    private suspend fun getAndUpdateCountryField(signupData: Map<StrigaSignupDataType, StrigaSignupData>) {
        // db stores COUNTRY_OF_BIRTH as country name code ISO 3166-1 alpha-3,
        // so we need to find country by code and convert to country name
        val selectedCountryValue = signupData[StrigaSignupDataType.COUNTRY_OF_BIRTH]?.value.orEmpty()
        interactor.findCountryByIsoAlpha3(selectedCountryValue)
            ?.also { onCountryChanged(it) }

        val selectedCountryCodeValue = signupData[StrigaSignupDataType.PHONE_CODE_WITH_PLUS]?.value ?: return
        selectedCountryCode = countryRepository.findCountryCodeByPhoneCode(selectedCountryCodeValue)
    }

    private fun mapDataForStorage() {
        setCachedData(StrigaSignupDataType.COUNTRY_OF_BIRTH, countryOfBirth?.nameCodeAlpha3.orEmpty())
    }

    private suspend fun loadDefaultCountryCode() {
        try {
            val countryCode: CountryCode = countryRepository.detectCountryOrDefault()
            selectedCountryCode = countryCode
            setCachedData(StrigaSignupDataType.PHONE_CODE_WITH_PLUS, countryCode.phoneCode)

            val selectedPhoneNumber = signupData[StrigaSignupDataType.PHONE_NUMBER]?.value

            view?.setupPhoneCountryCodePicker(
                selectedCountryCode = countryCode,
                selectedPhoneNumber = selectedPhoneNumber
            )
        } catch (e: Throwable) {
            Timber.e(e, "Loading default country code failed")
            view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
        }
    }

    override fun onCountryClicked() {
        view?.showCountryPicker()
    }

    private fun setCachedData(type: StrigaSignupDataType, newValue: String) {
        signupData[type] = StrigaSignupData(type = type, value = newValue)
    }
}

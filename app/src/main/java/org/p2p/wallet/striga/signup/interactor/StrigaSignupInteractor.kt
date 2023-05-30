package org.p2p.wallet.striga.signup.interactor

import timber.log.Timber
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.utils.unsafeLazy

typealias ValidationResult = Pair<Boolean, List<StrigaSignupFieldState>>

class StrigaSignupInteractor(
    private val validator: StrigaSignupDataValidator,
    private val countryRepository: CountryRepository,
    private val signupDataRepository: StrigaSignupDataLocalRepository
) {
    private val firstStepDataTypes: Set<StrigaSignupDataType> by unsafeLazy {
        setOf(
            StrigaSignupDataType.EMAIL,
            StrigaSignupDataType.PHONE_NUMBER,
            StrigaSignupDataType.FIRST_NAME,
            StrigaSignupDataType.LAST_NAME,
            StrigaSignupDataType.DATE_OF_BIRTH,
            StrigaSignupDataType.COUNTRY_OF_BIRTH
        )
    }

    private val secondStepDataTypes: Set<StrigaSignupDataType> by unsafeLazy {
        setOf(
            StrigaSignupDataType.OCCUPATION,
            StrigaSignupDataType.SOURCE_OF_FUNDS,
            StrigaSignupDataType.COUNTRY,
            StrigaSignupDataType.CITY,
            StrigaSignupDataType.CITY_ADDRESS_LINE,
            StrigaSignupDataType.CITY_POSTAL_CODE,
            StrigaSignupDataType.CITY_STATE
        )
    }

    fun validateFirstStep(data: Map<StrigaSignupDataType, StrigaSignupData>): ValidationResult {
        return validateStep(firstStepDataTypes, data)
    }

    fun validateSecondStep(data: Map<StrigaSignupDataType, StrigaSignupData>): ValidationResult {
        return validateStep(secondStepDataTypes, data)
    }

    suspend fun getSelectedCountry(): Country {
        // todo: get saved country
        return countryRepository.detectCountryOrDefault()
    }

    suspend fun findPhoneMaskByCountry(country: Country): String? {
        return countryRepository.findPhoneMaskByCountry(country)
    }

    suspend fun updateSignupData(data: StrigaSignupData) {
        signupDataRepository.updateSignupData(data)
    }

    suspend fun getSignupData(): List<StrigaSignupData> {
        return when (val data = signupDataRepository.getUserSignupData()) {
            is StrigaDataLayerResult.Success -> {
                data.value
            }
            is StrigaDataLayerResult.Failure -> {
                Timber.w(data.error, "Failed to get signup data")
                emptyList()
            }
        }
    }

    private fun validateStep(
        types: Set<StrigaSignupDataType>,
        data: Map<StrigaSignupDataType, StrigaSignupData>
    ): ValidationResult {
        val validationResults = types.map { data[it] ?: StrigaSignupData(it, null) }
            .map(::validate)

        val countValid = validationResults.count { it.isValid }
        val mustBeValid = types.size

        return (countValid == mustBeValid) to validationResults
    }

    private fun validate(data: StrigaSignupData): StrigaSignupFieldState {
        return validator.validate(data)
    }
}

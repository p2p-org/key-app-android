package org.p2p.wallet.striga.signup.interactor

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.utils.unsafeLazy

typealias ValidationResult = Pair<Boolean, List<StrigaSignupFieldState>>

class StrigaSignupInteractor(
    private val appScope: AppScope,
    private val validator: StrigaSignupDataValidator,
    private val countryRepository: CountryRepository,
    private val signupDataRepository: StrigaSignupDataLocalRepository
) {

    private val signupDataCache = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()
    private var needsSave = false

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

    fun notifyDataChanged(type: StrigaSignupDataType, newValue: String) {
        signupDataCache[type] = StrigaSignupData(type, newValue)
        needsSave = true
    }

    fun validateFirstStep(): ValidationResult {
        return validateStep(firstStepDataTypes)
    }

    fun validateSecondStep(): ValidationResult {
        return validateStep(secondStepDataTypes)
    }

    suspend fun getSelectedCountry(): Country {
        // todo: get saved country
        return countryRepository.detectCountryOrDefault()
    }

    suspend fun findPhoneMaskByCountry(country: Country): String? {
        return countryRepository.findPhoneMaskByCountry(country)
    }

    suspend fun getSignupData(): List<StrigaSignupData> {
        return when (val data = signupDataRepository.getUserSignupData()) {
            is StrigaDataLayerResult.Success -> {
                data.value
            }
            is StrigaDataLayerResult.Failure -> {
                Timber.e(data.error, "Striga signup data: failed to get")
                emptyList()
            }
        }
    }

    /**
     * This method saves data only if it was changed
     */
    fun saveChanges() {
        if (needsSave) {
            needsSave = false

            // use AppScope to make sure that data will be saved even if view scope has been cancelled
            appScope.launch {
                signupDataRepository.updateSignupData(signupDataCache.values)
            }
            Timber.d("Striga signup data: saved")
        } else {
            Timber.d("Striga signup data: nothing to save")
        }
    }

    private fun validateStep(types: Set<StrigaSignupDataType>): ValidationResult {
        val validationResults = types.map { signupDataCache[it] ?: StrigaSignupData(it, null) }.map(::validate)

        val countValid = validationResults.count { it.isValid }
        val mustBeValid = types.size

        return (countValid == mustBeValid) to validationResults
    }

    private fun validate(data: StrigaSignupData): StrigaSignupFieldState {
        return validator.validate(data)
    }
}

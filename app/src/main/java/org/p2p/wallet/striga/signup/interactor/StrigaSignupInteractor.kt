package org.p2p.wallet.striga.signup.interactor

import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.model.PhoneMask
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.InputValidator
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.utils.DateTimeUtils
import org.p2p.wallet.utils.unsafeLazy

typealias ValidationResult = Pair<Boolean, List<StrigaSignupFieldState>>

class StrigaSignupInteractor(
    private val appScope: AppScope,
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val validator: StrigaSignupDataValidator,
    private val countryRepository: CountryCodeRepository,
    private val signupDataRepository: StrigaSignupDataLocalRepository,
    private val userInteractor: StrigaUserInteractor,
    private val metadataInteractor: MetadataInteractor
) {
    private val firstStepDataTypes: Set<StrigaSignupDataType> by unsafeLazy {
        setOf(
            StrigaSignupDataType.EMAIL,
            StrigaSignupDataType.PHONE_CODE_WITH_PLUS,
            StrigaSignupDataType.PHONE_NUMBER,
            StrigaSignupDataType.FIRST_NAME,
            StrigaSignupDataType.LAST_NAME,
            StrigaSignupDataType.DATE_OF_BIRTH,
            StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3
        )
    }

    private val secondStepDataTypes: Set<StrigaSignupDataType> by unsafeLazy {
        setOf(
            StrigaSignupDataType.OCCUPATION,
            StrigaSignupDataType.SOURCE_OF_FUNDS,
            StrigaSignupDataType.COUNTRY_ALPHA_2,
            StrigaSignupDataType.CITY,
            StrigaSignupDataType.CITY_ADDRESS_LINE,
            StrigaSignupDataType.CITY_POSTAL_CODE,
            StrigaSignupDataType.CITY_STATE
        )
    }

    fun validateFirstStep(data: Map<StrigaSignupDataType, StrigaSignupData>): ValidationResult {
        return validateStep(data, firstStepDataTypes)
    }

    fun addValidator(inputValidator: InputValidator) {
        validator.addValidator(inputValidator)
    }

    fun validateSecondStep(data: Map<StrigaSignupDataType, StrigaSignupData>): ValidationResult {
        return validateStep(data, secondStepDataTypes)
    }

    suspend fun getSelectedCountry(): CountryCode {
        // todo: get saved country
        return countryRepository.detectCountryOrDefault()
    }

    suspend fun findCountryByIsoAlpha2(codeAlpha2: String?): CountryCode? {
        if (codeAlpha2.isNullOrBlank()) return null
        return countryRepository.findCountryCodeByIsoAlpha2(codeAlpha2)
    }

    suspend fun findCountryByIsoAlpha3(codeAlpha3: String?): CountryCode? {
        if (codeAlpha3.isNullOrBlank()) return null
        return countryRepository.findCountryCodeByIsoAlpha3(codeAlpha3)
    }

    suspend fun getSignupDataFirstStep(): List<StrigaSignupData> = getSignupData(firstStepDataTypes)
    suspend fun getSignupDataSecondStep(): List<StrigaSignupData> = getSignupData(secondStepDataTypes)

    suspend fun getSignupData(fields: Set<StrigaSignupDataType> = emptySet()): List<StrigaSignupData> {
        return when (val data = signupDataRepository.getUserSignupData()) {
            is StrigaDataLayerResult.Success -> {
                if (fields.isEmpty()) {
                    data.value
                } else {
                    data.value.filter { it.type in fields }
                }
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
    fun saveChanges(data: Collection<StrigaSignupData>) {
        // use AppScope to make sure that data will be saved even if view scope has been cancelled
        appScope.launch {
            signupDataRepository.updateSignupData(data)
        }
        Timber.d("Striga signup data: saved")
    }

    @Throws(IllegalStateException::class, StrigaDataLayerError::class)
    suspend fun createUser() {
        if (inAppFeatureFlags.strigaSimulateUserCreateFlag.featureValue) {
            delay(1000)
            return
        }
        // firstly, check if metadata is available
        metadataInteractor.currentMetadata
            ?: error("Metadata is not fetched")

        when (val result = userInteractor.createUser(getSignupData())) {
            is StrigaDataLayerResult.Success -> {
                updateMetadata(result.value.userId)
            }
            is StrigaDataLayerResult.Failure -> {
                throw result.error
            }
        }

        userInteractor.resendSmsForVerifyPhoneNumber().unwrap()
    }

    private fun validateStep(
        data: Map<StrigaSignupDataType, StrigaSignupData>,
        types: Set<StrigaSignupDataType>
    ): ValidationResult {
        val validationResults = types.map {
            data[it] ?: StrigaSignupData(type = it, value = null)
        }
            .map(::validate)

        val countValid = validationResults.count { it.isValid }
        val mustBeValid = types.size

        return (countValid == mustBeValid) to validationResults
    }

    private fun validate(data: StrigaSignupData): StrigaSignupFieldState = validator.validate(data)

    private suspend fun updateMetadata(userId: String) {
        val currentMetadata = metadataInteractor.currentMetadata ?: return
        val newMetadata = currentMetadata.copy(
            strigaMetadata = GatewayOnboardingMetadata.StrigaMetadata(
                userId = userId,
                userIdTimestamp = DateTimeUtils.getCurrentTimestampInSeconds()
            )
        )
        metadataInteractor.updateMetadata(newMetadata)
    }
}

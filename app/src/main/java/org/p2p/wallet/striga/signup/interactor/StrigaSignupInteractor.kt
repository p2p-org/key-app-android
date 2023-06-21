package org.p2p.wallet.striga.signup.interactor

import timber.log.Timber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.PhoneNumberWithCode
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.striga.model.StrigaApiErrorCode
import org.p2p.wallet.striga.model.StrigaApiErrorResponse
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.map
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.PhoneNumberInputValidator
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.utils.DateTimeUtils
import org.p2p.wallet.utils.unsafeLazy

typealias ValidationResult = Pair<Boolean, List<StrigaSignupFieldState>>

class StrigaSignupInteractor(
    private val appScope: AppScope,
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val validator: StrigaSignupDataValidator,
    private val countryCodeRepository: CountryCodeRepository,
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

    suspend fun loadAndSaveSignupData(): StrigaDataLayerResult<Unit> {
        return if (userInteractor.isUserCreated() && getSignupData().isEmpty()) {
            Timber.d("Striga signup data: loading from remote")
            loadAndSaveSignupDataFromRemote().map { }
        } else {
            StrigaDataLayerResult.Success(Unit)
        }
    }

    fun validateField(type: StrigaSignupDataType, value: String): StrigaSignupFieldState {
        return validator.validate(StrigaSignupData(type, value))
    }

    fun validateFirstStep(data: Map<StrigaSignupDataType, StrigaSignupData>): ValidationResult {
        return validateStep(data, firstStepDataTypes)
    }

    fun setPhoneValidator(inputValidator: PhoneNumberInputValidator) {
        validator.setPhoneValidator(inputValidator)
    }

    fun validateSecondStep(data: Map<StrigaSignupDataType, StrigaSignupData>): ValidationResult {
        return validateStep(data, secondStepDataTypes)
    }

    suspend fun getSelectedCountry(): CountryCode {
        // todo: get saved country
        return countryCodeRepository.detectCountryOrDefault()
    }

    fun findCountryByIsoAlpha2(codeAlpha2: String?): CountryCode? {
        if (codeAlpha2.isNullOrBlank()) return null
        return countryCodeRepository.findCountryCodeByIsoAlpha2(codeAlpha2)
    }

    fun findCountryByIsoAlpha3(codeAlpha3: String?): CountryCode? {
        if (codeAlpha3.isNullOrBlank()) return null
        return countryCodeRepository.findCountryCodeByIsoAlpha3(codeAlpha3)
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

    fun saveChanges(data: Collection<StrigaSignupData>) {
        // use AppScope to make sure that data will be saved even if view scope has been cancelled
        appScope.launch {
            signupDataRepository.updateSignupData(data)
        }
        Timber.d("Striga signup data: saved")
    }

    suspend fun saveChangesNow(data: Collection<StrigaSignupData>) {
        signupDataRepository.updateSignupData(data)
    }

    @Throws(IllegalStateException::class, StrigaDataLayerError::class)
    suspend fun createUser() {
        if (inAppFeatureFlags.strigaSimulateMobileAlreadyVerifiedFlag.featureValue) {
            throw StrigaDataLayerError.ApiServiceError.PhoneNumberAlreadyUsed(
                StrigaApiErrorResponse(400, StrigaApiErrorCode.MOBILE_ALREADY_VERIFIED, "phone number already used")
            )
        }
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

        // not critical, we can ignore errors from this call
        kotlin.runCatching {
            userInteractor.loadAndSaveUserStatusData().unwrap()
        }
        userInteractor.resendSmsForVerifyPhoneNumber().unwrap()
    }

    /**
     * Retrieves phone number with code from metadata or from cached data
     * @param cachedPhoneNumber - phone number from cached data
     * @param cachedPhoneCode - phone code from cached data
     * @return [PhoneNumberWithCode] with phone number and phone code;
     * !! pay attention that returning phone number can be an empty string
     */
    suspend fun retrievePhoneNumberWithCode(
        cachedPhoneCode: String?,
        cachedPhoneNumber: String?
    ): PhoneNumberWithCode {
        // 1. getting cached number
        var selectedPhoneNumber = cachedPhoneNumber

        // 2. getting cached phone code
        var phoneCode = cachedPhoneCode?.let {
            countryCodeRepository.findCountryCodeByPhoneCode(it)
        }

        // 3. if cached code is null, attempting to get phone code from metadata
        // if cached phone number is not set, then we will use metadata phone number
        val metadata = metadataInteractor.currentMetadata
        if (phoneCode == null && metadata != null) {
            check(metadata.customSharePhoneNumberE164.isNotBlank()) { "Metadata phone number can't be empty" }
            val parsePhoneResult = countryCodeRepository.parsePhoneNumber(metadata.customSharePhoneNumberE164)
            phoneCode = parsePhoneResult?.phoneCode
            selectedPhoneNumber = selectedPhoneNumber.takeIf {
                it?.isNotBlank() == true
            } ?: parsePhoneResult?.phoneNumberNational
        }

        // 4. if phone code still is not detected, then getting default one
        phoneCode = phoneCode ?: countryCodeRepository.detectCountryOrDefault()

        // 5. if phone number is not either detected or cached, then it will be an empty string
        return PhoneNumberWithCode(phoneCode, selectedPhoneNumber.orEmpty())
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

    private suspend fun loadAndSaveSignupDataFromRemote(): StrigaDataLayerResult<StrigaUserDetails> {
        return when (val userDetails = userInteractor.getUserDetails()) {
            is StrigaDataLayerResult.Success<StrigaUserDetails> -> {
                val signupData = userDetails.value.toSignupData()
                signupDataRepository.updateSignupData(signupData)
                userDetails
            }
            is StrigaDataLayerResult.Failure -> {
                Timber.e(userDetails.error, "Unable to load striga user details")
                userDetails
            }
        }
    }
}

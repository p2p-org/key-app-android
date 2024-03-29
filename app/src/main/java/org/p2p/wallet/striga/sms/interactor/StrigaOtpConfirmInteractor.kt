package org.p2p.wallet.striga.sms.interactor

import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.auth.model.PhoneNumberWithCode
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.smsinput.SmsInputTimer
import org.p2p.wallet.striga.common.model.StrigaApiErrorCode
import org.p2p.wallet.striga.common.model.StrigaApiErrorResponse
import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.common.model.toFailureResult
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.sms.StrigaSmsApiCaller
import org.p2p.wallet.striga.user.storage.StrigaStorageContract
import org.p2p.wallet.utils.DateTimeUtils

private val EXCEEDED_VERIFICATION_ATTEMPTS_TIMEOUT_MILLIS = 1.days.inWholeMilliseconds
private val EXCEEDED_RESEND_ATTEMPTS_TIMEOUT_MILLIS = 1.days.inWholeMilliseconds

private const val SMS_RESEND_MOCK_LIMIT = 6

private val SMS_ERROR_RESEND_EXCEEDED: StrigaDataLayerResult.Failure<Unit> =
    StrigaDataLayerError.ApiServiceError(
        response = StrigaApiErrorResponse(
            httpStatus = 400,
            internalErrorCode = StrigaApiErrorCode.EXCEEDED_DAILY_RESEND_SMS_LIMIT,
            details = "EXCEEDED_DAILY_RESEND_SMS_LIMIT"
        )
    ).toFailureResult()

private val SMS_ERROR_VERIFICATIONS_EXCEEDED: StrigaDataLayerResult.Failure<Unit> =
    StrigaDataLayerError.ApiServiceError(
        response = StrigaApiErrorResponse(
            httpStatus = 400,
            internalErrorCode = StrigaApiErrorCode.EXCEEDED_VERIFICATION_ATTEMPTS,
            details = "EXCEEDED_VERIFICATION_ATTEMPTS"
        )
    ).toFailureResult()

class StrigaOtpConfirmInteractor(
    private val strigaSignupDataRepository: StrigaSignupDataLocalRepository,
    private val phoneCodeRepository: CountryCodeRepository,
    private val inAppFeatureFlags: InAppFeatureFlags,
    private val smsInputTimer: SmsInputTimer,
    private val strigaStorage: StrigaStorageContract,
    private val smsApiCaller: StrigaSmsApiCaller
) {
    val timer: Flow<Int> get() = smsInputTimer.smsInputTimerFlow
    val isTimerNotActive: Boolean get() = !smsInputTimer.isTimerActive

    fun launchInitialTimer() {
        smsInputTimer.startSmsInputTimerFlow()
    }

    suspend fun getUserPhoneCodeToPhoneNumber(): PhoneNumberWithCode {
        val userSignupData = strigaSignupDataRepository.getUserSignupDataAsMap()
            .unwrap()
        return getPhoneNumberWithCodeFromLocal(userSignupData)
    }

    private fun getPhoneNumberWithCodeFromLocal(
        userSignupData: Map<StrigaSignupDataType, StrigaSignupData>
    ): PhoneNumberWithCode {
        val phoneCode = userSignupData[StrigaSignupDataType.PHONE_CODE_WITH_PLUS]?.value
            ?.let { phoneCodeRepository.findCountryCodeByPhoneCode(it) }
            ?: error("Failed to find phone code in signup details")
        val phoneNumber = userSignupData[StrigaSignupDataType.PHONE_NUMBER]?.value
            ?: error("Failed to find phone number in signup details")
        return PhoneNumberWithCode(phoneCode, phoneNumber)
    }

    suspend fun validateSms(smsCode: String): StrigaDataLayerResult<Unit> {
        return if (inAppFeatureFlags.strigaSmsVerificationMockFlag.featureValue) {
            mockVerifyPhoneNumber(smsCode)
        } else {
            smsApiCaller.verifySms(smsCode)
        }
    }

    fun getExceededLimitsErrorIfPresent(): StrigaDataLayerResult.Failure<Unit>? {
        return when {
            // avoid api call to show error immediately
            isExceededVerificationAttempts() -> SMS_ERROR_VERIFICATIONS_EXCEEDED
            isExceededResendAttempts() -> SMS_ERROR_RESEND_EXCEEDED
            else -> null
        }
    }

    suspend fun resendSms(): StrigaDataLayerResult<Unit> {
        if (inAppFeatureFlags.strigaSmsVerificationMockFlag.featureValue) {
            smsInputTimer.startSmsInputTimerFlow()
            return if (smsInputTimer.smsResendCount > SMS_RESEND_MOCK_LIMIT) {
                SMS_ERROR_RESEND_EXCEEDED
            } else {
                StrigaDataLayerResult.Success(Unit)
            }
        }

        return getExceededLimitsErrorIfPresent() ?: smsApiCaller.resendSms()
            .onTypedFailure<StrigaDataLayerError.ApiServiceError> {
                when (it.errorCode) {
                    StrigaApiErrorCode.EXCEEDED_VERIFICATION_ATTEMPTS -> {
                        setExceededVerificationAttempts()
                    }
                    StrigaApiErrorCode.EXCEEDED_DAILY_RESEND_SMS_LIMIT -> {
                        setExceededResendAttempts()
                    }
                    else -> Unit
                }
            }
            .onSuccess {
                smsInputTimer.startSmsInputTimerFlow()
            }
    }

    private fun setExceededVerificationAttempts() {
        strigaStorage.smsExceededVerificationAttemptsMillis = System.currentTimeMillis()
    }

    private fun setExceededResendAttempts() {
        strigaStorage.smsExceededResendAttemptsMillis = System.currentTimeMillis()
    }

    private fun isExceededVerificationAttempts(): Boolean {
        return !DateTimeUtils.isTimestampExpired(
            strigaStorage.smsExceededVerificationAttemptsMillis,
            EXCEEDED_VERIFICATION_ATTEMPTS_TIMEOUT_MILLIS
        )
    }

    private fun isExceededResendAttempts(): Boolean {
        return !DateTimeUtils.isTimestampExpired(
            strigaStorage.smsExceededResendAttemptsMillis,
            EXCEEDED_RESEND_ATTEMPTS_TIMEOUT_MILLIS
        )
    }

    private fun mockVerifyPhoneNumber(smsCode: String): StrigaDataLayerResult<Unit> {
        return when (smsCode) {
            "123456" -> StrigaDataLayerResult.Success(Unit)
            "000000" -> {
                SMS_ERROR_VERIFICATIONS_EXCEEDED
            }
            "000001" -> {
                SMS_ERROR_RESEND_EXCEEDED
            }
            "000002" -> {
                StrigaDataLayerError.InternalError(
                    cause = IllegalStateException("Mocked Internal error")
                ).toFailureResult()
            }
            else -> {
                StrigaDataLayerError.ApiServiceError(
                    response = StrigaApiErrorResponse(
                        httpStatus = 400,
                        internalErrorCode = StrigaApiErrorCode.INVALID_VERIFICATION_CODE,
                        details = "INVALID_VERIFICATION_CODE"
                    )
                ).toFailureResult()
            }
        }
    }
}

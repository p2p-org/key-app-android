package org.p2p.wallet.smsinput.striga

import org.p2p.wallet.auth.model.PhoneNumberWithCode
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.striga.model.StrigaApiErrorCode
import org.p2p.wallet.striga.model.StrigaApiErrorResponse
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toFailureResult
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.user.repository.StrigaUserRepository

class StrigaSmsInputInteractor(
    private val strigaUserRepository: StrigaUserRepository,
    private val strigaSignupDataRepository: StrigaSignupDataLocalRepository,
    private val phoneCodeRepository: CountryCodeRepository,
    private val inAppFeatureFlags: InAppFeatureFlags,
) {

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
            strigaUserRepository.verifyPhoneNumber(smsCode)
        }
    }

    suspend fun resendSms(): StrigaDataLayerResult<Unit> =
        strigaUserRepository.resendSmsForVerifyPhoneNumber()

    private fun mockVerifyPhoneNumber(smsCode: String): StrigaDataLayerResult<Unit> {
        return when (smsCode) {
            "123456" -> StrigaDataLayerResult.Success(Unit)
            "000000" -> {
                StrigaDataLayerError.ApiServiceError(
                    response = StrigaApiErrorResponse(
                        httpStatus = 400,
                        internalErrorCode = StrigaApiErrorCode.EXCEEDED_VERIFICATION_ATTEMPTS,
                        details = "EXCEEDED_VERIFICATION_ATTEMPTS"
                    )
                ).toFailureResult()
            }
            "000001" -> {
                StrigaDataLayerError.ApiServiceError(
                    response = StrigaApiErrorResponse(
                        httpStatus = 400,
                        internalErrorCode = StrigaApiErrorCode.EXCEEDED_DAILY_RESEND_SMS_LIMIT,
                        details = "EXCEEDED_DAILY_RESEND_SMS_LIMIT"
                    )
                ).toFailureResult()
            }
            "000002" -> {
                // todo: remove
                StrigaDataLayerError.InternalError(
                    cause = IllegalStateException("This error was moved to user creation flow")
                ).toFailureResult()
            }
            "000003" -> {
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

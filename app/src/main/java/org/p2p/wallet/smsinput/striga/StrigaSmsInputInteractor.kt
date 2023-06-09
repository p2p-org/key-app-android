package org.p2p.wallet.smsinput.striga

import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.user.repository.StrigaUserRepository

class StrigaSmsInputInteractor(
    private val strigaUserRepository: StrigaUserRepository,
    private val strigaSignupDataRepository: StrigaSignupDataLocalRepository,
    private val phoneCodeRepository: CountryCodeRepository,
) {

    suspend fun getUserPhoneCodeToPhoneNumber(): Pair<String, String> {
        val userSignupData = strigaSignupDataRepository.getUserSignupDataAsMap().unwrap()
        val phoneCode = userSignupData[StrigaSignupDataType.PHONE_CODE_WITH_PLUS]?.value
            ?: error("Failed to find phone code")
        val phoneNumber = userSignupData[StrigaSignupDataType.PHONE_NUMBER]?.value
            ?: error("Failed to find phone number")
        return phoneCode to phoneNumber
    }

    fun getUserPhoneMask(phoneCode: String): String? {
        return phoneCodeRepository.findCountryCodeByPhoneCode(phoneCode)?.mask
    }

    suspend fun validateSms(smsCode: String): StrigaDataLayerResult<Unit> =
        strigaUserRepository.verifyPhoneNumber(smsCode)

    suspend fun resendSms(): StrigaDataLayerResult<Unit> =
        strigaUserRepository.resendSmsForVerifyPhoneNumber()
}

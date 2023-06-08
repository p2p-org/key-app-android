package org.p2p.wallet.smsinput.striga

import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.map
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.user.repository.StrigaUserRepository

class StrigaSmsInputInteractor(
    private val strigaUserRepository: StrigaUserRepository,
    private val strigaSignupDataRepository: StrigaSignupDataLocalRepository,
    private val phoneCodeRepository: CountryCodeRepository,
) {

    suspend fun getUserPhoneCodeToPhoneNumber(): StrigaDataLayerResult<Pair<String, String>> {
        return strigaSignupDataRepository.getUserSignupData()
            .map { fields ->
                fields.first { it.type == StrigaSignupDataType.PHONE_CODE_WITH_PLUS }.value to
                    fields.first { it.type == StrigaSignupDataType.PHONE_NUMBER }.value
            }
            .map { it.first.orEmpty() to it.second.orEmpty() }
    }

    fun getUserPhoneMask(phoneCode: String): String? {
        return phoneCodeRepository.findCountryCodeByPhoneCode(phoneCode)?.mask
    }

    suspend fun validateSms(smsCode: String): StrigaDataLayerResult<Unit> =
        strigaUserRepository.verifyPhoneNumber(smsCode)

    suspend fun resendSms(): StrigaDataLayerResult<Unit> =
        strigaUserRepository.resendSmsForVerifyPhoneNumber()
}

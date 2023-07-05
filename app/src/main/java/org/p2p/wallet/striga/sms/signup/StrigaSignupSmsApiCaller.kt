package org.p2p.wallet.striga.sms.signup

import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.sms.StrigaSmsApiCaller
import org.p2p.wallet.striga.user.repository.StrigaUserRepository

class StrigaSignupSmsApiCaller(
    private val strigaUserRepository: StrigaUserRepository
) : StrigaSmsApiCaller {
    override suspend fun resendSms(): StrigaDataLayerResult<Unit> {
        return strigaUserRepository.resendSmsForVerifyPhoneNumber()
    }

    override suspend fun verifySms(smsCode: String): StrigaDataLayerResult<Unit> {
        return strigaUserRepository.verifyPhoneNumber(smsCode)
    }
}

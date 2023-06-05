package org.p2p.wallet.smsinput.striga

import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.user.repository.StrigaUserRepository

class StrigaSmsInputInteractor(
    private val strigaUserRepository: StrigaUserRepository
) {
    suspend fun validateSms(smsCode: String): StrigaDataLayerResult<Unit> =
        strigaUserRepository.verifyPhoneNumber(smsCode)

    suspend fun resendSms(): StrigaDataLayerResult<Unit> =
        strigaUserRepository.resendSmsForVerifyPhoneNumber()
}

package org.p2p.wallet.striga.user.interactor

import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.map
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
import org.p2p.wallet.striga.user.repository.StrigaUserRepository

class StrigaUserInteractor(
    private val userRepository: StrigaUserRepository
) {

    suspend fun createUser(data: List<StrigaSignupData>): StrigaDataLayerResult<StrigaUserInitialDetails> {
        return userRepository.createUser(data)
    }

    suspend fun getUserDetails(): StrigaDataLayerResult<StrigaUserDetails> {
        return userRepository.getUserDetails()
    }

    suspend fun verifyPhoneNumber(verificationCode: String): StrigaDataLayerResult<Unit> {
        return userRepository.verifyPhoneNumber(verificationCode)
    }

    suspend fun resendSmsForVerifyPhoneNumber(): StrigaDataLayerResult<Unit> {
        return userRepository.resendSmsForVerifyPhoneNumber()
    }

    suspend fun isMobileVerified(): StrigaDataLayerResult<Boolean> {
        return getUserDetails().map { it.kycDetails.isMobileVerified }
    }
}

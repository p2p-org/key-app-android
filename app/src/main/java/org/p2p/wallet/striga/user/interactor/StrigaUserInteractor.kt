package org.p2p.wallet.striga.user.interactor

import org.p2p.wallet.kyc.model.StrigaKycStatusBanner
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
import org.p2p.wallet.striga.user.model.StrigaUserStatusDestination
import org.p2p.wallet.striga.user.repository.StrigaUserRepository
import org.p2p.wallet.striga.user.repository.StrigaUserStatusRepository

class StrigaUserInteractor(
    private val userRepository: StrigaUserRepository,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val userStatusRepository: StrigaUserStatusRepository
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

    fun isUserCreated(): Boolean {
        return strigaUserIdProvider.getUserId() != null
    }
    fun loadAndSaveUserStatusData() {
        userStatusRepository.loadUserKycStatus()
    }

    fun getUserStatusBanner(): StrigaKycStatusBanner? {
        return userStatusRepository.getBanner()
    }

    fun getUserDestination(): StrigaUserStatusDestination {
        return userStatusRepository.getUserDestination()
    }
}
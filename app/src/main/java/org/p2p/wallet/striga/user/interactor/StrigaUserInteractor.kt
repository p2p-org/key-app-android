package org.p2p.wallet.striga.user.interactor

import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.user.StrigaStorageContract
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
import org.p2p.wallet.striga.user.model.StrigaUserStatus
import org.p2p.wallet.striga.user.repository.StrigaUserRepository

class StrigaUserInteractor(
    private val userRepository: StrigaUserRepository,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val strigaStorage: StrigaStorageContract,
) {
    suspend fun loadAndSaveUserData() {
        if (!isUserCreated()) {
            return
        }
    }

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

    fun getSavedUserStatus(): StrigaUserStatus? {
        return strigaStorage.userStatus
    }

    fun isUserCreated(): Boolean {
        return strigaUserIdProvider.getUserId() != null
    }
}

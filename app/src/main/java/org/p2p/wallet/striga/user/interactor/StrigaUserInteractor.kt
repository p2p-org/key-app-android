package org.p2p.wallet.striga.user.interactor

import timber.log.Timber
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.model.StrigaUserStatus
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.user.StrigaStorageContract
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
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
        val userStatus = getUserStatus()
        saveUserStatus(userStatus)
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

    /**
     * @return null if user is not created
     */
    suspend fun getUserStatus(): StrigaUserStatus? {
        if (!isUserCreated()) {
            return null
        }
        return when (val userDetails = getUserDetails()) {
            is StrigaDataLayerResult.Success<StrigaUserDetails> -> {
                StrigaUserStatus(
                    isMobileVerified = userDetails.value.kycDetails.isMobileVerified,
                    kycStatus = userDetails.value.kycDetails.kycStatus
                )
            }
            is StrigaDataLayerResult.Failure -> {
                Timber.e(userDetails.error, "Unable to get striga user status")
                null
            }
        }
    }

    fun isUserCreated(): Boolean {
        return strigaUserIdProvider.getUserId() != null
    }

    private fun saveUserStatus(status: StrigaUserStatus?) {
        strigaStorage.userStatus = status
        Timber.d("Save striga user status: $status")
    }
}

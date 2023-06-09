package org.p2p.wallet.striga.user.interactor

import timber.log.Timber
import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.map
import org.p2p.wallet.striga.signup.model.StrigaUserStatus
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus
import org.p2p.wallet.striga.user.repository.StrigaUserRepository

class StrigaUserInteractor(
    private val userRepository: StrigaUserRepository,
    private val strigaUserIdProvider: StrigaUserIdProvider,
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

    suspend fun getUserStatus(): StrigaUserStatus {
        if (!isUserCreated()) {
            return StrigaUserStatus(
                isUserCreated = false,
                isMobileVerified = false,
                StrigaUserVerificationStatus.NOT_STARTED
            )
        }
        return when (val userDetails = getUserDetails()) {
            is StrigaDataLayerResult.Success<StrigaUserDetails> -> {
                StrigaUserStatus(
                    isUserCreated = true,
                    isMobileVerified = userDetails.value.kycDetails.isMobileVerified,
                    kycStatus = userDetails.value.kycDetails.kycStatus
                )
            }
            is StrigaDataLayerResult.Failure -> {
                Timber.d("Unable to get striga user status: ${userDetails.error.message}")
                StrigaUserStatus(
                    isUserCreated = true,
                    isMobileVerified = false,
                    kycStatus = StrigaUserVerificationStatus.NOT_STARTED
                )
            }
        }
    }

    private fun isUserCreated(): Boolean {
        return strigaUserIdProvider.getUserId() != null
    }
}

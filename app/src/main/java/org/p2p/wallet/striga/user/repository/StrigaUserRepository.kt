package org.p2p.wallet.striga.user.repository

import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
import org.p2p.wallet.striga.user.model.StrigaUserStatusDetails
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus

interface StrigaUserRepository {
    suspend fun createUser(data: List<StrigaSignupData>): StrigaDataLayerResult<StrigaUserInitialDetails>
    suspend fun getUserDetails(): StrigaDataLayerResult<StrigaUserDetails>
    suspend fun getUserVerificationStatus(): StrigaDataLayerResult<StrigaUserStatusDetails>
    suspend fun verifyPhoneNumber(verificationCode: String): StrigaDataLayerResult<Unit>
    suspend fun resendSmsForVerifyPhoneNumber(): StrigaDataLayerResult<Unit>

    suspend fun getAccessToken(): StrigaDataLayerResult<String>

    suspend fun simulateUserStatus(status: StrigaUserVerificationStatus): StrigaDataLayerResult<Unit>
}

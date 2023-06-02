package org.p2p.wallet.striga.user.repository

import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.user.model.StrigaUserDetails
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails

interface StrigaUserRepository {
    suspend fun createUser(data: List<StrigaSignupData>): StrigaDataLayerResult<StrigaUserInitialDetails>
    suspend fun getUserDetails(): StrigaDataLayerResult<StrigaUserDetails>
    suspend fun verifyPhoneNumber(verificationCode: String): StrigaDataLayerResult<Unit>
}

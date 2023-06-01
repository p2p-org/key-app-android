package org.p2p.wallet.striga.user.repository

import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.user.model.StrigaUserDetails

interface StrigaUserRepository {
    suspend fun getUserDetails(): StrigaDataLayerResult<StrigaUserDetails>
    suspend fun verifyPhoneNumber(verificationCode: String): StrigaDataLayerResult<Unit>
}

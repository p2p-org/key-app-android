package org.p2p.wallet.striga.signup.repository

import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.model.StrigaDataLayerResult

interface StrigaSignupDataLocalRepository {
    suspend fun getUserSignupData(): StrigaDataLayerResult<List<StrigaSignupData>>
    suspend fun createUserSignupData(): StrigaDataLayerResult<Unit>
    suspend fun updateSignupData(newData: StrigaSignupData): StrigaDataLayerResult<Unit>
    suspend fun updateSignupData(newData: Collection<StrigaSignupData>): StrigaDataLayerResult<Unit>
}

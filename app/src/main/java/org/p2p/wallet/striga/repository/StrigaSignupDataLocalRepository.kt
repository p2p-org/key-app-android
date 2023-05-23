package org.p2p.wallet.striga.repository

import org.p2p.wallet.striga.model.StrigaSignupData

interface StrigaSignupDataLocalRepository {
    suspend fun getUserSignupData(): StrigaDataLayerResult<List<StrigaSignupData>>
    suspend fun createUserSignupData(): StrigaDataLayerResult<Unit>
    suspend fun updateSignupData(newData: StrigaSignupData): StrigaDataLayerResult<Unit>
}

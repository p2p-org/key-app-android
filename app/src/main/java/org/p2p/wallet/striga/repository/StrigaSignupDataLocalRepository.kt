package org.p2p.wallet.striga.repository

import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaSignupData
import org.p2p.wallet.striga.repository.dao.StrigaSignupDataEntity

interface StrigaSignupDataLocalRepository {
    suspend fun getUserSignupData(): StrigaDataLayerResult<List<StrigaSignupDataEntity>, StrigaDataLayerError>
    suspend fun createUserSignupData(): StrigaDataLayerResult<Unit, StrigaDataLayerError>
    suspend fun updateSignupData(newData: StrigaSignupData): StrigaDataLayerResult<Unit, StrigaDataLayerError>
}

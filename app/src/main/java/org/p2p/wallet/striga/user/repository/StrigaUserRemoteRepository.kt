package org.p2p.wallet.striga.user.repository

import org.p2p.wallet.striga.StrigaUserIdProvider
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toFailureResult
import org.p2p.wallet.striga.model.toSuccessResult
import org.p2p.wallet.striga.user.api.StrigaApi
import org.p2p.wallet.striga.user.model.StrigaUserDetails

class StrigaUserRemoteRepository(
    private val api: StrigaApi,
    private val strigaUserIdProvider: StrigaUserIdProvider,
    private val mapper: StrigaUserRepositoryMapper
) : StrigaUserRepository {
    override fun getUserDetails(): StrigaDataLayerResult<StrigaUserDetails> {
        return try {
            val response = api.getUserDetails(strigaUserIdProvider.getUserId())
            mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            StrigaDataLayerError.InternalError(error).toFailureResult()
        }
    }
}

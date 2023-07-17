package org.p2p.wallet.striga.exchange.repository.impl

import org.p2p.wallet.striga.exchange.api.StrigaExchangeApi
import org.p2p.wallet.striga.exchange.models.StrigaExchangePairsWithRates
import org.p2p.wallet.striga.exchange.repository.StrigaExchangeRepository
import org.p2p.wallet.striga.exchange.repository.mapper.StrigaExchangeRepositoryMapper
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toSuccessResult

class StrigaExchangeRemoteRepository(
    private val api: StrigaExchangeApi,
    private val mapper: StrigaExchangeRepositoryMapper,
) : StrigaExchangeRepository {

    override suspend fun getExchangeRates(): StrigaDataLayerResult<StrigaExchangePairsWithRates> {
        return try {
            val response = api.getExchangeRates()
            mapper.fromNetwork(response).toSuccessResult()
        } catch (error: Throwable) {
            StrigaDataLayerError.from(
                error = error,
                default = StrigaDataLayerError.InternalError(error)
            )
        }
    }
}

package org.p2p.wallet.striga.exchange.repository.impl

import org.p2p.wallet.striga.common.model.StrigaDataLayerError
import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.common.model.map
import org.p2p.wallet.striga.common.model.toSuccessResult
import org.p2p.wallet.striga.exchange.api.StrigaExchangeApi
import org.p2p.wallet.striga.exchange.models.StrigaExchangePairsWithRates
import org.p2p.wallet.striga.exchange.repository.StrigaExchangeRepository
import org.p2p.wallet.striga.exchange.repository.mapper.StrigaExchangeRepositoryMapper

class StrigaExchangeRemoteRepository(
    private val api: StrigaExchangeApi,
    private val mapper: StrigaExchangeRepositoryMapper,
) : StrigaExchangeRepository {

    class StrigaExchangeRateNotFound : Throwable()

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

    override suspend fun getExchangeRateForPair(
        fromTokenSymbol: String,
        toTokenSymbol: String
    ): StrigaDataLayerResult<StrigaExchangePairsWithRates.Rate> {
        return getExchangeRates().map {
            it.findRate(fromTokenSymbol, toTokenSymbol) ?: throw StrigaExchangeRateNotFound()
        }
    }
}

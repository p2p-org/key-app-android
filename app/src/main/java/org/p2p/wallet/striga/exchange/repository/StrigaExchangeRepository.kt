package org.p2p.wallet.striga.exchange.repository

import org.p2p.wallet.striga.common.model.StrigaDataLayerResult
import org.p2p.wallet.striga.exchange.models.StrigaExchangePairsWithRates

interface StrigaExchangeRepository {

    suspend fun getExchangeRates(): StrigaDataLayerResult<StrigaExchangePairsWithRates>

    /**
     * @return [StrigaExchangePairsWithRates.Rate] for given pair of tokens. Order of from/to tokens doesn't matter.
     */
    suspend fun getExchangeRateForPair(
        fromTokenSymbol: String,
        toTokenSymbol: String
    ): StrigaDataLayerResult<StrigaExchangePairsWithRates.Rate>
}

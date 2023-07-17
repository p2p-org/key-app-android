package org.p2p.wallet.striga.exchange.repository

import org.p2p.wallet.striga.exchange.models.StrigaExchangePairsWithRates
import org.p2p.wallet.striga.model.StrigaDataLayerResult

interface StrigaExchangeRepository {

    suspend fun getExchangeRates(): StrigaDataLayerResult<StrigaExchangePairsWithRates>
}

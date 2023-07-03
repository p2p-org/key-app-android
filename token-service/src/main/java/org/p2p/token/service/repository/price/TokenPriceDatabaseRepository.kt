package org.p2p.token.service.repository.price

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.token.service.database.TokenPriceDao
import org.p2p.token.service.database.mapper.TokenServiceDatabaseMapper
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

internal class TokenPriceDatabaseRepository(
    private val tokenPriceDao: TokenPriceDao,
    private val converter: TokenServiceDatabaseMapper,
    coroutineDispatchers: CoroutineDispatchers
) : TokenPriceLocalRepository, CoroutineScope {

    override val coroutineContext: CoroutineContext = coroutineDispatchers.io
    override fun setTokensPrice(prices: List<TokenServicePrice>) {
        launch {
            val entities = prices.mapNotNull { converter.toEntity(it) }
            tokenPriceDao.insertTokenPrice(entities)
        }
    }

    override suspend fun findTokenPriceByAddress(
        address: String
    ): TokenServicePrice? {
        val found = tokenPriceDao.getTokenPriceByAddress(address)
        return found?.let { converter.fromEntity(it) }
    }

    override suspend fun attachToTokensPrice(networkChain: TokenServiceNetwork): Flow<List<TokenServicePrice>> {
        return tokenPriceDao.getTokensRatesByNetworkFlow(networkChain.networkName)
            .map { it.map(converter::fromEntity) }
    }
}

package org.p2p.token.service.repository.price

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.p2p.core.utils.Constants
import org.p2p.token.service.database.TokenPriceDao
import org.p2p.token.service.database.mapper.TokenServiceDatabaseMapper
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

internal class TokenPriceDatabaseRepository(
    private val tokenPriceDao: TokenPriceDao,
    private val converter: TokenServiceDatabaseMapper,
) : TokenPriceLocalRepository {

    override suspend fun saveTokensPrice(prices: List<TokenServicePrice>) {
        val entities = prices.mapNotNull { converter.toEntity(it) }
        tokenPriceDao.insertTokenPrice(entities)
    }

    override suspend fun findTokenPriceByAddress(
        address: String,
        networkChain: TokenServiceNetwork
    ): TokenServicePrice? {
        val columnKey = address.toPriceColumnKey(networkChain.networkName)
        val found = tokenPriceDao.getTokenPriceByAddress(columnKey)
        return found?.let { converter.fromEntity(it) }
    }

    override fun observeTokenPrices(networkChain: TokenServiceNetwork): Flow<List<TokenServicePrice>> {
        return tokenPriceDao.getTokensRatesByNetworkFlow(networkChain.networkName)
            .map { it.map(converter::fromEntity) }
    }

    override suspend fun getLocalTokenPrices(): List<TokenServicePrice> {
        return tokenPriceDao.getAllTokensPrices()
            .map(converter::fromEntity)
    }
}

internal fun String.toPriceColumnKey(networkChainName: String): String {
    return if (this == Constants.WRAPPED_SOL_MINT) {
        "${Constants.TOKEN_SERVICE_NATIVE_SOL_TOKEN}_$networkChainName"
    } else {
        "${this}_$networkChainName"
    }
}

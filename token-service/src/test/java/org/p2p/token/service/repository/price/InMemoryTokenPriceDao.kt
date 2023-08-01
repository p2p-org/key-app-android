package org.p2p.token.service.repository.price

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import org.p2p.token.service.database.TokenPriceDao
import org.p2p.token.service.database.entity.TokenServicePriceEntity

internal class InMemoryTokenPriceDao : TokenPriceDao {

    private var data = listOf<TokenServicePriceEntity>()
        set(value) {
            field = value.distinctBy(TokenServicePriceEntity::tokenAddress)
        }

    private var flowData = MutableSharedFlow<List<TokenServicePriceEntity>>(1)

    init {
        flowData.tryEmit(data)
    }

    override suspend fun insertTokenPrice(entity: TokenServicePriceEntity) {
        data = data.plus(entity)
        flowData.emit(data)
    }

    override suspend fun insertTokenPrice(entities: List<TokenServicePriceEntity>) {
        data = data.plus(entities)
        flowData.emit(data)
    }

    override suspend fun getAllTokensPrices(): List<TokenServicePriceEntity> {
        return data
    }

    override fun getAllTokensPricesFlow(): Flow<List<TokenServicePriceEntity>> {
        return flowOf()
    }

    override suspend fun getTokenPriceByAddress(tokenColumnKey: String): TokenServicePriceEntity? {
        return data.find { it.tokenRateKey == tokenColumnKey }
    }

    override suspend fun getTokensRatesByNetwork(networkChain: String): List<TokenServicePriceEntity> {
        return data.filter { it.networkChainName == networkChain }
    }

    override fun getTokensRatesByNetworkFlow(networkChain: String): Flow<List<TokenServicePriceEntity>> {
        return flowData
    }
}

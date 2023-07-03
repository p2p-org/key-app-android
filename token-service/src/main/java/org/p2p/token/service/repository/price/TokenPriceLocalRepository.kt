package org.p2p.token.service.repository.price

import kotlinx.coroutines.flow.Flow
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

interface TokenPriceLocalRepository {
    fun saveTokensPrice(prices: List<TokenServicePrice>)
    suspend fun findTokenPriceByAddress( address: String): TokenServicePrice?
    suspend fun observeTokenPrices(networkChain: TokenServiceNetwork): Flow<List<TokenServicePrice>>
}

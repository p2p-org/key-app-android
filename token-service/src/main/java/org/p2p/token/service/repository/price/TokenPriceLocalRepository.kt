package org.p2p.token.service.repository.price

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

interface TokenPriceLocalRepository {
    fun setTokensPrice(prices: List<TokenServicePrice>)
    suspend fun findTokenPriceByAddress( address: String): TokenServicePrice?
    suspend fun attachToTokensPrice(networkChain: TokenServiceNetwork): Flow<List<TokenServicePrice>>
}

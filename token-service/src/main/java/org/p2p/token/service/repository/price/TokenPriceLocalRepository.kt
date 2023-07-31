package org.p2p.token.service.repository.price

import kotlinx.coroutines.flow.Flow
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

internal interface TokenPriceLocalRepository {
    suspend fun saveTokensPrice(prices: List<TokenServicePrice>)
    suspend fun findTokenPriceByAddress(address: String, networkChain: TokenServiceNetwork): TokenServicePrice?
    fun observeTokenPrices(networkChain: TokenServiceNetwork): Flow<List<TokenServicePrice>>
    suspend fun getLocalTokenPrices(): List<TokenServicePrice>
}

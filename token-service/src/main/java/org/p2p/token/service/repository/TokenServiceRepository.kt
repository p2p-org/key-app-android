package org.p2p.token.service.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

interface TokenServiceRepository {
    suspend fun loadPriceForTokens(chain: TokenServiceNetwork, tokenAddresses: List<String>)
    suspend fun loadMetadataForTokens(chain: TokenServiceNetwork, tokenAddresses: List<String>)
    suspend fun observeTokenPricesFlow(networkChain: TokenServiceNetwork): Flow<List<TokenServicePrice>>
    suspend fun findTokenPriceByAddress(tokenAddress: String): TokenServicePrice?
    suspend fun fetchTokenPriceByAddress(networkChain: TokenServiceNetwork, tokenAddress: String): TokenServicePrice?
    fun findTokenMetadataByAddress(networkChain: TokenServiceNetwork, tokenAddress: String): TokenServiceMetadata?
}

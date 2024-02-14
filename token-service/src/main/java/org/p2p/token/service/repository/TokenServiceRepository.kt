package org.p2p.token.service.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

interface TokenServiceRepository {
    suspend fun fetchTokenPricesForTokens(
        chain: TokenServiceNetwork,
        tokenAddresses: List<String>
    )
    suspend fun fetchMetadataForTokens(
        chain: TokenServiceNetwork,
        tokenAddresses: List<String>
    ): List<TokenServiceMetadata>

    fun observeTokenPricesFlow(networkChain: TokenServiceNetwork): Flow<List<TokenServicePrice>>
    suspend fun getTokenPriceByAddress(
        tokenAddress: String,
        networkChain: TokenServiceNetwork,
        forceRemote: Boolean = false
    ): TokenServicePrice?

    suspend fun getTokenPricesByAddress(
        tokenAddress: List<String>,
        networkChain: TokenServiceNetwork,
        forceRemote: Boolean = false
    ): List<TokenServicePrice>

    fun findTokenMetadataByAddress(
        networkChain: TokenServiceNetwork,
        tokenAddress: String
    ): TokenServiceMetadata?
}

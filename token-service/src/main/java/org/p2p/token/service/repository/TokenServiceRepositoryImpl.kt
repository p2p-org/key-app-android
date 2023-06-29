package org.p2p.token.service.repository

import kotlinx.coroutines.flow.StateFlow
import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.repository.metadata.TokenMetadataLocalRepository
import org.p2p.token.service.repository.metadata.TokenMetadataRepository
import org.p2p.token.service.repository.price.TokenPriceLocalRepository
import org.p2p.token.service.repository.price.TokenPriceRepository

class TokenServiceRepositoryImpl(
    private val priceRemoteRepository: TokenPriceRepository,
    private val priceLocalRepository: TokenPriceLocalRepository,
    private val metadataRemoteRepository: TokenMetadataRepository,
    private val metadataLocalRepository: TokenMetadataLocalRepository,
) : TokenServiceRepository {

    override suspend fun loadPriceForTokens(chain: TokenServiceNetwork, tokenAddresses: List<String>) {
        val result = priceRemoteRepository.loadTokensPrice(
            chain = chain,
            addresses = tokenAddresses
        )
        result.forEach { queryResult ->
            priceLocalRepository.setTokensPrice(
                networkChain = queryResult.networkChain,
                prices = queryResult.items
            )
        }
    }

    override suspend fun loadMetadataForTokens(chain: TokenServiceNetwork, tokenAddresses: List<String>) {
        val result = metadataRemoteRepository.loadTokensMetadata(
            chain = chain,
            addresses = tokenAddresses
        )
        result.forEach { queryResult ->
            metadataLocalRepository.setTokensMetadata(
                networkChain = queryResult.networkChain,
                metadata = queryResult.items
            )
        }
    }

    override fun getTokenPricesFlow(networkChain: TokenServiceNetwork): StateFlow<Map<String, TokenServicePrice>> =
        priceLocalRepository.attachToTokensPrice(networkChain)

    override fun findTokenPriceByAddress(
        networkChain: TokenServiceNetwork,
        tokenAddress: String
    ): TokenServicePrice? {
        return priceLocalRepository.findTokenPriceByAddress(networkChain = networkChain, address = tokenAddress)
    }

    override suspend fun fetchTokenPriceByAddress(
        networkChain: TokenServiceNetwork,
        tokenAddress: String
    ): TokenServicePrice? {
        loadPriceForTokens(chain = networkChain, tokenAddresses = listOf(tokenAddress))
        return findTokenPriceByAddress(networkChain = networkChain, tokenAddress = tokenAddress)
    }

    override fun findTokenMetadataByAddress(
        networkChain: TokenServiceNetwork,
        tokenAddress: String
    ): TokenServiceMetadata? {
        return metadataLocalRepository.findTokenMetadataByAddress(networkChain = networkChain, address = tokenAddress)
    }
}

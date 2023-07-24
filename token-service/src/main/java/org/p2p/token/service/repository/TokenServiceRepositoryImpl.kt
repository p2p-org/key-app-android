package org.p2p.token.service.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.repository.metadata.TokenMetadataLocalRepository
import org.p2p.token.service.repository.metadata.TokenMetadataRepository
import org.p2p.token.service.repository.price.TokenPriceLocalRepository
import org.p2p.token.service.repository.price.TokenPriceRepository

internal class TokenServiceRepositoryImpl(
    private val priceRemoteRepository: TokenPriceRepository,
    private val priceLocalRepository: TokenPriceLocalRepository,
    private val metadataLocalRepository: TokenMetadataLocalRepository,
    private val metadataRemoteRepository: TokenMetadataRepository
) : TokenServiceRepository {

    override suspend fun loadPriceForTokens(chain: TokenServiceNetwork, tokenAddresses: List<String>) {
        val result = priceRemoteRepository.loadTokensPrice(
            chain = chain,
            addresses = tokenAddresses
        )
        val tokensPrices = result.flatMap { it.items }
        priceLocalRepository.saveTokensPrice(tokensPrices)
    }

    override suspend fun loadMetadataForTokens(
        chain: TokenServiceNetwork,
        tokenAddresses: List<String>
    ): List<TokenServiceMetadata> {
        val result = metadataRemoteRepository.loadTokensMetadata(
            chain = chain,
            addresses = tokenAddresses
        )
        val tokensMetadata = result.flatMap { it.items }
        metadataLocalRepository.saveTokensMetadata(tokensMetadata)
        return tokensMetadata
    }

    override suspend fun observeTokenPricesFlow(networkChain: TokenServiceNetwork): Flow<List<TokenServicePrice>> =
        priceLocalRepository.observeTokenPrices(networkChain)

    override suspend fun findTokenPriceByAddress(
        tokenAddress: String
    ): TokenServicePrice? {
        return priceLocalRepository.findTokenPriceByAddress(address = tokenAddress)
    }

    override suspend fun fetchTokenPriceByAddress(
        networkChain: TokenServiceNetwork,
        tokenAddress: String
    ): TokenServicePrice? {
        loadPriceForTokens(chain = networkChain, tokenAddresses = listOf(tokenAddress))
        return findTokenPriceByAddress(tokenAddress = tokenAddress)
    }

    override fun findTokenMetadataByAddress(
        networkChain: TokenServiceNetwork,
        tokenAddress: String
    ): TokenServiceMetadata? {
        return metadataLocalRepository.findTokenMetadataByAddress(networkChain = networkChain, address = tokenAddress)
    }
}

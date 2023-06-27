package org.p2p.token.service.interactor

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.repository.metadata.TokenMetadataLocalRepository
import org.p2p.token.service.repository.metadata.TokenMetadataRemoteRepository
import org.p2p.token.service.repository.price.TokenPriceLocalRepository
import org.p2p.token.service.repository.price.TokenPriceRemoteRepository

internal class TokenServiceInteractor(
    private val priceRemoteRepository: TokenPriceRemoteRepository,
    private val priceLocalRepository: TokenPriceLocalRepository,
    private val metadataRemoteRepository: TokenMetadataRemoteRepository,
    private val metadataLocalRepository: TokenMetadataLocalRepository
) {

    suspend fun loadPriceForTokens(chain: TokenServiceNetwork, tokenAddresses: List<String>) {
        val result = priceRemoteRepository.loadTokensPrice(chain = chain, addresses = tokenAddresses)
        result.forEach { queryResult ->
            priceLocalRepository.setTokensPrice(
                networkChain = queryResult.networkChain,
                prices = queryResult.items
            )
        }
    }

    suspend fun loadMetadataForTokens(chain: TokenServiceNetwork, tokenAddresses: List<String>) {
        val result = metadataRemoteRepository.loadTokensMetadata(chain = chain, addresses = tokenAddresses)
        result.forEach { queryResult ->
            metadataLocalRepository.setTokensMetadata(
                networkChain = queryResult.networkChain,
                metadata = queryResult.items
            )
        }
    }

    fun findTokenPriceByAddress(networkChain: TokenServiceNetwork, tokenAddress: String): TokenServicePrice? {
        return priceLocalRepository.findTokenPriceByAddress(networkChain = networkChain, address = tokenAddress)
    }

    fun findTokenMetadataByAddress(networkChain: TokenServiceNetwork, tokenAddress: String): TokenServiceMetadata? {
        return metadataLocalRepository.findTokenMetadataByAddress(networkChain = networkChain, address = tokenAddress)
    }
}

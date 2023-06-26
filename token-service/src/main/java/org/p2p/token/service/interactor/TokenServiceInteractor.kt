package org.p2p.token.service.interactor

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.repository.TokenServiceLocalRepository
import org.p2p.token.service.repository.TokenServiceRemoteRepository

internal class TokenServiceInteractor(
    private val remoteRepository: TokenServiceRemoteRepository,
    private val localRepository: TokenServiceLocalRepository
) {

    suspend fun loadPriceForTokens(chain: TokenServiceNetwork, tokenAddresses: List<String>) {
        val result = remoteRepository.loadTokensPrice(chain = chain, addresses = tokenAddresses)
        result.forEach { queryResult ->
            localRepository.setTokensPrice(
                networkChain = queryResult.networkChain,
                prices = queryResult.items
            )
        }
    }

    suspend fun loadMetadataForTokens(chain: TokenServiceNetwork, tokenAddresses: List<String>) {
        val result = remoteRepository.loadTokensMetadata(chain = chain, addresses = tokenAddresses)
        result.forEach { queryResult ->
            localRepository.setTokensMetadata(
                networkChain = queryResult.networkChain,
                metadata = queryResult.items
            )
        }
    }

    fun findTokenPriceByAddress(networkChain: TokenServiceNetwork, tokenAddress: String): TokenServicePrice? {
        return localRepository.findTokenPriceByAddress(networkChain = networkChain, address = tokenAddress)
    }

    fun findTokenMetadataByAddress(networkChain: TokenServiceNetwork, tokenAddress: String): TokenServiceMetadata? {
        return localRepository.findTokenMetadataByAddress(networkChain = networkChain, address = tokenAddress)
    }
}

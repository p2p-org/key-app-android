package org.p2p.token.service.repository

import timber.log.Timber
import kotlinx.coroutines.flow.Flow
import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.repository.metadata.TokenMetadataLocalRepository
import org.p2p.token.service.repository.metadata.TokenMetadataRepository
import org.p2p.token.service.repository.price.JupiterTokenPriceRepository
import org.p2p.token.service.repository.price.TokenPriceLocalRepository
import org.p2p.token.service.repository.price.TokenPriceRepository

internal class TokenServiceRepositoryImpl(
    private val priceRemoteRepository: TokenPriceRepository,
    private val jupiterPriceRepository: JupiterTokenPriceRepository,
    private val priceLocalRepository: TokenPriceLocalRepository,
    private val metadataLocalRepository: TokenMetadataLocalRepository,
    private val metadataRemoteRepository: TokenMetadataRepository
) : TokenServiceRepository {

    @Deprecated("Use getTokenPriceByAddress")
    override suspend fun fetchTokenPricesForTokens(chain: TokenServiceNetwork, tokenAddresses: List<String>) {
//        val result = priceRemoteRepository.loadTokensPrice(
//            chain = chain,
//            addresses = tokenAddresses
//        )
//        val tokensPrices = result.flatMap { it.items }
//        priceLocalRepository.saveTokensPrice(tokensPrices)
    }

    override suspend fun fetchMetadataForTokens(
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

    override fun observeTokenPricesFlow(networkChain: TokenServiceNetwork): Flow<List<TokenServicePrice>> =
        priceLocalRepository.observeTokenPrices(networkChain)

    override suspend fun getTokenPriceByAddress(
        tokenAddress: String,
        networkChain: TokenServiceNetwork,
        forceRemote: Boolean
    ): TokenServicePrice? {
        return getTokenPricesByAddresses(listOf(tokenAddress), networkChain).firstOrNull()
    }

    override suspend fun getTokenPricesByAddresses(
        tokenAddress: List<String>,
        networkChain: TokenServiceNetwork,
        forceRemote: Boolean
    ): List<TokenServicePrice> = try {
        when (networkChain) {
            TokenServiceNetwork.SOLANA -> {
                jupiterPriceRepository.loadTokensPrice(networkChain, tokenAddress).items
            }
            TokenServiceNetwork.ETHEREUM -> {
                priceRemoteRepository.loadTokensPrice(networkChain, tokenAddress).flatMap { it.items }
            }
        }
    } catch (error: Exception) {
        Timber.i(error)
        emptyList()
    }

    override suspend fun getTokenPricesByAddressAsMap(
        tokenAddress: List<String>,
        networkChain: TokenServiceNetwork,
        forceRemote: Boolean
    ): Map<String, TokenServicePrice> {
        return getTokenPricesByAddresses(tokenAddress, networkChain, forceRemote)
            .associateBy { it.tokenAddress }
    }

    override fun findTokenMetadataByAddress(
        networkChain: TokenServiceNetwork,
        tokenAddress: String
    ): TokenServiceMetadata? {
        return metadataLocalRepository.findTokenMetadataByAddress(networkChain = networkChain, address = tokenAddress)
    }
}

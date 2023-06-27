package org.p2p.token.service.repository

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

private const val TAG = "MarketPriceLocalRepository"

internal class TokenServiceInMemoryRepository : TokenServiceLocalRepository {
    private val tokensPriceMap = mutableMapOf<TokenServiceNetwork, Map<String, TokenServicePrice>>()
    private val tokensMetadataMap = mutableMapOf<TokenServiceNetwork, Map<String, TokenServiceMetadata>>()

    override fun setTokensPrice(networkChain: TokenServiceNetwork, prices: List<TokenServicePrice>) {
        tokensPriceMap[networkChain] = prices.associateBy { it.address }
    }

    override fun findTokenPriceByAddress(networkChain: TokenServiceNetwork, address: String): TokenServicePrice? {
        val tokenPrice = tokensPriceMap[networkChain]
        return tokenPrice?.get(address)
    }

    override fun setTokensMetadata(networkChain: TokenServiceNetwork, metadata: List<TokenServiceMetadata>) {
        tokensMetadataMap[networkChain] = metadata.associateBy { it.address }
    }

    override fun findTokenMetadataByAddress(networkChain: TokenServiceNetwork, address: String): TokenServiceMetadata? {
        val tokenMetadata = tokensMetadataMap[networkChain]
        return tokenMetadata?.get(address)
    }
}

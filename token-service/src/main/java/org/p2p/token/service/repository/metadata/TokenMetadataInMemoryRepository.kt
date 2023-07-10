package org.p2p.token.service.repository.metadata

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork

class TokenMetadataInMemoryRepository : TokenMetadataLocalRepository {

    private val tokensMetadataMap = mutableMapOf<TokenServiceNetwork, Map<String, TokenServiceMetadata>>()

    override fun setTokensMetadata(networkChain: TokenServiceNetwork, metadata: List<TokenServiceMetadata>) {
        tokensMetadataMap[networkChain] = metadata.associateBy { it.address }
    }

    override fun findTokenMetadataByAddress(networkChain: TokenServiceNetwork, address: String): TokenServiceMetadata? {
        val tokenMetadata = tokensMetadataMap[networkChain]
        return tokenMetadata?.get(address)
    }
}

package org.p2p.token.service.repository.metadata

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork

class TokenMetadataInMemoryRepository : TokenMetadataLocalRepository {

    private val tokensMetadataList = mutableSetOf<TokenServiceMetadata>()

    override fun saveTokensMetadata(metadata: List<TokenServiceMetadata>) {
        tokensMetadataList.addAll(metadata)
    }

    override fun findTokenMetadataByAddress(networkChain: TokenServiceNetwork, address: String): TokenServiceMetadata? {
        return tokensMetadataList.firstOrNull { it.chain == networkChain && it.address == address }
    }
}

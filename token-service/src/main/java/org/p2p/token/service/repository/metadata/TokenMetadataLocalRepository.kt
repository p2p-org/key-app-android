package org.p2p.token.service.repository.metadata

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork

interface TokenMetadataLocalRepository {
    fun saveTokensMetadata(metadata: List<TokenServiceMetadata>)
    fun findTokenMetadataByAddress(networkChain: TokenServiceNetwork, address: String): TokenServiceMetadata?
}

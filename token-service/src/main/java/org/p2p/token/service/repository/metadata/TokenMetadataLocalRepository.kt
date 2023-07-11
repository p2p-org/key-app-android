package org.p2p.token.service.repository.metadata

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork

interface TokenMetadataLocalRepository {
    fun setTokensMetadata(networkChain: TokenServiceNetwork, metadata: List<TokenServiceMetadata>)
    fun findTokenMetadataByAddress(networkChain: TokenServiceNetwork, address: String): TokenServiceMetadata?
}

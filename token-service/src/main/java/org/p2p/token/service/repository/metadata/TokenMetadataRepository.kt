package org.p2p.token.service.repository.metadata

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServiceQueryResult

interface TokenMetadataRepository {
    suspend fun loadTokensMetadata(
        chain: TokenServiceNetwork,
        addresses: List<String>
    ): List<TokenServiceQueryResult<TokenServiceMetadata>>
}

package org.p2p.token.service.repository.metadata

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServiceQueryResult
import org.p2p.token.service.model.UpdateTokenMetadataResult

interface TokenMetadataRepository {
    suspend fun loadSolTokensMetadata(ifModifiedSince: String?): UpdateTokenMetadataResult
    suspend fun loadTokensMetadata(
        chain: TokenServiceNetwork,
        addresses: List<String>
    ): List<TokenServiceQueryResult<TokenServiceMetadata>>
}

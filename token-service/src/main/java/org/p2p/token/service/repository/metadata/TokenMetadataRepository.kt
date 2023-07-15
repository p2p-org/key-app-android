package org.p2p.token.service.repository.metadata

import org.p2p.token.service.model.UpdateTokenMetadataResult

interface TokenMetadataRepository {
    suspend fun loadTokensMetadata(ifModifiedSince: String?): UpdateTokenMetadataResult
}

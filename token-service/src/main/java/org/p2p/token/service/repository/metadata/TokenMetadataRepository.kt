package org.p2p.token.service.repository.metadata

import org.p2p.token.service.model.TokenMetadataResult

interface TokenMetadataRepository {
    suspend fun loadTokensMetadata(lastModified: String?): TokenMetadataResult
}

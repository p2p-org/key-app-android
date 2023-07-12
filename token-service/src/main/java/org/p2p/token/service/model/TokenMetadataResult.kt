package org.p2p.token.service.model

import org.p2p.core.token.TokenMetadata

sealed interface TokenMetadataResult {

    object NoUpdate : TokenMetadataResult

    data class NewMetadata(val tokensMetadata: TokenMetadata) : TokenMetadataResult

    data class Error(val throwable: Throwable) : TokenMetadataResult
}

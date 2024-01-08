package org.p2p.token.service.model

import org.p2p.core.token.TokensMetadataInfo

sealed interface UpdateTokenMetadataResult {

    object NoUpdate : UpdateTokenMetadataResult

    data class NewMetadata(val remoteTokensMetadata: TokensMetadataInfo) : UpdateTokenMetadataResult

    data class Error(val throwable: Throwable) : UpdateTokenMetadataResult
}

package org.p2p.token.service.api.events.manager

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServicePrice

sealed interface TokenServiceUpdate {
    object Loading : TokenServiceUpdate
    data class TokensPriceLoaded(val result: List<TokenServicePrice>) : TokenServiceUpdate
    data class TokensMetadataLoaded(val result: List<TokenServiceMetadata>) : TokenServiceUpdate
    object Idle : TokenServiceUpdate
}

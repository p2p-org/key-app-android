package org.p2p.token.service.api.events.manager

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServicePrice

sealed interface TokenServiceEvent {
    object Loading : TokenServiceEvent
    data class TokensPriceLoaded(val result: List<TokenServicePrice>) : TokenServiceEvent
    data class TokensMetadataLoaded(val result: List<TokenServiceMetadata>) : TokenServiceEvent
    object Idle : TokenServiceEvent
}


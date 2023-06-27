package org.p2p.token.service.manager

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServicePrice

sealed interface TokenServiceEvent {
    object Loading : TokenServiceEvent
    data class TokensPrice(val result: Map<String,TokenServicePrice>) : TokenServiceEvent
    data class TokensMetadata(val result: Map<String,TokenServiceMetadata>) : TokenServiceEvent
    object Idle : TokenServiceEvent
}


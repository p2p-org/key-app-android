package org.p2p.token.service.api.events.manager

import org.p2p.token.service.model.TokenServiceNetwork

interface TokenServiceEventSubscriber {
    fun onUpdate(eventType: TokenServiceEventType, event: TokenServiceEvent)
}

enum class TokenServiceEventType {
    SOLANA_CHAIN_EVENT,
    ETHEREUM_CHAIN_EVENT;

    companion object {
        fun from(networkChain: TokenServiceNetwork): TokenServiceEventType {
            return when(networkChain) {
                TokenServiceNetwork.ETHEREUM -> ETHEREUM_CHAIN_EVENT
                TokenServiceNetwork.SOLANA -> SOLANA_CHAIN_EVENT
            }
        }
    }
}

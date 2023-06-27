package org.p2p.token.service.manager

class TokenServiceEventManager {

    private val listeners = mutableSetOf<TokenServiceEventListener>()

    fun subscribe(listener: TokenServiceEventListener) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: TokenServiceEventListener) {
        listeners.remove(listener)
    }

    fun notify(eventType: TokenServiceEventType, event: TokenServiceEvent) {
        listeners.forEach { it.onUpdate(eventType, event) }
    }
}

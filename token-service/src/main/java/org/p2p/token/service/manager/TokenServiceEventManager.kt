package org.p2p.token.service.manager

import timber.log.Timber

private const val TAG = "TokenServiceEventManager"

class TokenServiceEventManager {

    private val listeners = mutableSetOf<TokenServiceEventListener>()

    fun subscribe(listener: TokenServiceEventListener) {
        Timber.tag(TAG).d("New subscriber added = $listener")
        listeners.add(listener)
    }

    fun unsubscribe(listener: TokenServiceEventListener) {
        listeners.remove(listener)
    }

    fun notify(eventType: TokenServiceEventType, event: TokenServiceEvent) {
        Timber.tag(TAG).d("Event received = $eventType, event = $event")
        listeners.forEach { it.onUpdate(eventType, event) }
    }
}
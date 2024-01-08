package org.p2p.token.service.api.events.manager

import timber.log.Timber

private const val TAG = "TokenServiceEventManager"

class TokenServiceEventManager {

    private val listeners = mutableSetOf<TokenServiceEventSubscriber>()

    fun subscribe(listener: TokenServiceEventSubscriber) {
        Timber.tag(TAG).d("New subscriber added = $listener")
        listeners.add(listener)
    }

    fun unsubscribe(listener: TokenServiceEventSubscriber) {
        listeners.remove(listener)
    }

    fun notify(eventType: TokenServiceEventType, data: TokenServiceUpdate) {
        Timber.tag(TAG).d("Event received = $eventType, data = $data")
        listeners.forEach { it.onUpdate(eventType, data) }
    }
}

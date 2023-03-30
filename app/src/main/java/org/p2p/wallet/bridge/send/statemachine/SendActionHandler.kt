package org.p2p.wallet.bridge.send.statemachine

import kotlinx.coroutines.flow.Flow

interface SendActionHandler {

    fun canHandle(
        newEvent: SendFeatureAction,
    ): Boolean

    fun handle(
        newAction: SendFeatureAction,
    ): Flow<SendState>
}

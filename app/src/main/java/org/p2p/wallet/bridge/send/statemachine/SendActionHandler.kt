package org.p2p.wallet.bridge.send.statemachine

import kotlinx.coroutines.flow.Flow

interface SendActionHandler {

    fun canHandle(
        newEvent: SendFeatureAction,
        staticState: SendState.Static,
    ): Boolean

    fun handle(
        lastStaticState: SendState.Static,
        newAction: SendFeatureAction,
    ): Flow<SendState>
}

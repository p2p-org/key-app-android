package org.p2p.wallet.bridge.send.statemachine

import kotlinx.coroutines.flow.Flow

interface SendActionHandler {

    fun canHandle(
        newEvent: SendFeatureAction,
        staticState: BridgeSendState.Static,
    ): Boolean

    fun handle(
        currentState: BridgeSendState,
        newAction: SendFeatureAction,
    ): Flow<BridgeSendState>
}

package org.p2p.wallet.bridge.send.statemachine

import kotlinx.coroutines.flow.Flow

interface BridgeSendActionHandler {

    fun canHandle(
        newEvent: BridgeSendAction,
        staticState: BridgeSendState.Static,
    ): Boolean

    fun handle(
        currentState: BridgeSendState,
        newAction: BridgeSendAction,
    ): Flow<BridgeSendState>
}

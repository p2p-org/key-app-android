package org.p2p.wallet.newsend.statemachine

import kotlinx.coroutines.flow.Flow

interface SendActionHandler {

    fun canHandle(
        newEvent: SendFeatureAction,
        staticState: SendState,
    ): Boolean

    fun handle(
        lastStaticState: SendState.Static,
        newAction: SendFeatureAction,
    ): Flow<SendState>
}

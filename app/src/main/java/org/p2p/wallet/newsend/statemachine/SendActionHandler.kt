package org.p2p.wallet.newsend.statemachine

import kotlinx.coroutines.flow.MutableStateFlow

interface SendActionHandler {

    fun canHandle(
        newEvent: SendFeatureAction,
        staticState: SendState,
    ): Boolean

    suspend fun handle(
        stateFlow: MutableStateFlow<SendState>,
        staticState: SendState.Static,
        newAction: SendFeatureAction,
    )
}

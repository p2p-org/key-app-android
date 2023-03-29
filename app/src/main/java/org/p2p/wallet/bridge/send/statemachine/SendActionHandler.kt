package org.p2p.wallet.bridge.send.statemachine

interface SendActionHandler {

    fun canHandle(
        newEvent: SendFeatureAction,
    ): Boolean

    suspend fun handle(
        newAction: SendFeatureAction,
    ): SendState
}

package org.p2p.wallet.auth.statemachine

class RestoreStateMachine {

    private lateinit var currentState: RestoreState

    fun updateState(currentState: RestoreState) {
        this.currentState = currentState
    }

    fun getSocialFlow() = currentState.onSocialFlow()

    fun getCustomFlow() = currentState.onCustomFlow()
}

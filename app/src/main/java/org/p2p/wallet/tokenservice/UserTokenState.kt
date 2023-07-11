package org.p2p.wallet.tokenservice

import org.p2p.core.token.Token

sealed interface UserTokenState {
    object Loading : UserTokenState
    object Refreshing : UserTokenState
    data class Loaded(
        val solTokens: List<Token.Active>,
        val ethTokens: List<Token.Eth>
    ) : UserTokenState

    object Empty : UserTokenState
    data class Error(val cause: Throwable) : UserTokenState
    object Idle : UserTokenState

    fun isLoading(): Boolean = this is Loading || this is Refreshing
}

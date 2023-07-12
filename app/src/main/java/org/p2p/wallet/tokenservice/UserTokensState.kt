package org.p2p.wallet.tokenservice

import org.p2p.core.token.Token

sealed interface UserTokensState {
    object Loading : UserTokensState
    object Refreshing : UserTokensState
    data class Loaded(
        val solTokens: List<Token.Active>,
        val ethTokens: List<Token.Eth>
    ) : UserTokensState

    object Empty : UserTokensState
    data class Error(val cause: Throwable) : UserTokensState
    object Idle : UserTokensState

    fun isLoading(): Boolean = this is Loading || this is Refreshing
}

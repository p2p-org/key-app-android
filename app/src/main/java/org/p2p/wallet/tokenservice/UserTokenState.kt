package org.p2p.wallet.tokenservice

import org.p2p.core.token.Token

sealed interface UserTokenState {
    object Loading : UserTokenState
    object Refreshing : UserTokenState
    data class Loaded(
        val solTokens: List<Token.Active>,
        val ethTokens: List<Token.Eth>,

        @Deprecated("workaround, we need to move this logic further")
        val isSellAvailable: Boolean
    ) : UserTokenState

    data class Empty(val emptyStateTokens: List<Token.Other>) : UserTokenState
    data class Error(val cause: Throwable) : UserTokenState
    object Idle : UserTokenState

    fun isLoading(): Boolean = this is Loading || this is Refreshing
}

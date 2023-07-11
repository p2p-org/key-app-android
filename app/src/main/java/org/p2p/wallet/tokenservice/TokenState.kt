package org.p2p.wallet.tokenservice

import org.p2p.core.token.Token

sealed interface TokenState {
    object Loading : TokenState
    object Refreshing : TokenState
    data class Loaded(val solTokens: List<Token.Active>, val ethTokens: List<Token.Eth>) : TokenState
    data class Error(val cause: Throwable) : TokenState
}

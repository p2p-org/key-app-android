package org.p2p.wallet.tokenservice.model

import org.p2p.core.token.Token

sealed interface SolanaTokenLoadState {
    object Loading : SolanaTokenLoadState
    object Refreshing : SolanaTokenLoadState
    object Idle : SolanaTokenLoadState
    data class Error(val throwable: Throwable) : SolanaTokenLoadState
    data class Loaded(val tokens: List<Token.Active>) : SolanaTokenLoadState
}

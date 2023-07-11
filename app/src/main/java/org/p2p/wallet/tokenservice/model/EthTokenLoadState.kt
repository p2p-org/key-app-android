package org.p2p.wallet.tokenservice.model

import org.p2p.core.token.Token

sealed interface EthTokenLoadState {
    object Loading : EthTokenLoadState
    object Refreshing : EthTokenLoadState
    object Idle : EthTokenLoadState
    data class Error(val throwable: Throwable) : EthTokenLoadState
    data class Loaded(val tokens: List<Token.Eth>) : EthTokenLoadState
}

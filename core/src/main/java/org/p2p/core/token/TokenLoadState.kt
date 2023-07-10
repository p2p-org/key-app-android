package org.p2p.core.token

sealed class TokenLoadState {
    object Loading : TokenLoadState()
    data class Loaded(val data: List<Token>) : TokenLoadState()
    object RequestRates : TokenLoadState()
    data class RateLoaded(val data: List<Token>) : TokenLoadState()
    object Idle : TokenLoadState()
}

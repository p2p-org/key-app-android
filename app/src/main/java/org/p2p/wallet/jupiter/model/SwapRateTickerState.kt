package org.p2p.wallet.jupiter.model

sealed interface SwapRateTickerState {
    data class Shown(val newRate: String) : SwapRateTickerState
    object Loading : SwapRateTickerState
    object Hidden : SwapRateTickerState
}

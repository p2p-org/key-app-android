package org.p2p.wallet.jupiter.model

sealed interface SwapRateTickerState {
    data class Shown(val formattedNewRate: String) : SwapRateTickerState
    object Loading : SwapRateTickerState
    object Hidden : SwapRateTickerState
}

package org.p2p.wallet.solana.model

sealed interface SolanaNetworkState {

    data class Online(val averageTps: Int) : SolanaNetworkState

    object ShowError : SolanaNetworkState

    object Idle : SolanaNetworkState
}

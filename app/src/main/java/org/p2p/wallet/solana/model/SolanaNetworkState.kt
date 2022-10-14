package org.p2p.wallet.solana.model

sealed interface SolanaNetworkState {

    data class Online(val averageTps: Int) : SolanaNetworkState

    object Offline : SolanaNetworkState

    object Idle : SolanaNetworkState
}

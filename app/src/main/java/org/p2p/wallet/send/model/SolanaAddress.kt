package org.p2p.wallet.send.model

sealed interface SolanaAddress {
    object NewAccountNeeded : SolanaAddress
    object AccountExists : SolanaAddress
    object InvalidAddress : SolanaAddress
}

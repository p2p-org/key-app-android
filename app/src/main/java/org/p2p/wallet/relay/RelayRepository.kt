package org.p2p.wallet.relay

import org.p2p.solanaj.model.types.RecentBlockhash

interface RelayRepository {
    suspend fun signTransaction(transaction: String, keypair: String, blockhash: RecentBlockhash): String
}

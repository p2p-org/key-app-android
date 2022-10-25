package org.p2p.wallet.relay

interface RelayRepository {
    suspend fun signTransaction(transaction: String, keypair: String, blockhash: String): String
}

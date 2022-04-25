package org.p2p.wallet.history.repository.remote

import org.p2p.solanaj.kits.transaction.TransactionDetails

interface TransactionDetailsRemoteRepository {
    suspend fun getTransactions(signatures: List<String>): List<TransactionDetails>
}

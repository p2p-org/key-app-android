package org.p2p.wallet.history.repository.remote

import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.wallet.history.interactor.stream.HistoryStreamItem

interface TransactionDetailsRemoteRepository {
    suspend fun getTransactions(
        userPublicKey: String,
        transactionSignatures: List<HistoryStreamItem>
    ): List<TransactionDetails>
}

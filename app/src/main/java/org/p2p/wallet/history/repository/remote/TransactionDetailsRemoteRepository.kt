package org.p2p.wallet.history.repository.remote

import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.wallet.history.model.RpcTransactionSignature

interface TransactionDetailsRemoteRepository {
    suspend fun getTransactions(
        userPublicKey: String,
        transactionSignatures: List<RpcTransactionSignature>
    ): List<TransactionDetails>
}

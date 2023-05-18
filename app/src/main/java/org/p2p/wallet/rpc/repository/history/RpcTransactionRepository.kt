package org.p2p.wallet.rpc.repository.history

import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.model.types.ConfirmationStatus

/**
 * All transactions are being sent only in BASE64 encoding here
 * */
interface RpcTransactionRepository {
    suspend fun sendTransaction(
        transaction: Transaction,
        preflightCommitment: ConfirmationStatus = ConfirmationStatus.FINALIZED
    ): String

    suspend fun sendTransaction(
        serializedTransaction: String,
        preflightCommitment: ConfirmationStatus = ConfirmationStatus.FINALIZED
    ): String

    suspend fun simulateTransaction(
        transaction: Transaction,
        preflightCommitment: ConfirmationStatus = ConfirmationStatus.FINALIZED
    ): String

    suspend fun simulateTransaction(
        serializedTransaction: String,
        preflightCommitment: ConfirmationStatus = ConfirmationStatus.FINALIZED
    ): String
}

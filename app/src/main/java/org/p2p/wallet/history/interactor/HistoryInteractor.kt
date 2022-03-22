package org.p2p.wallet.history.interactor

import org.p2p.solanaj.model.types.SignatureInformationResponse
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.repository.TransactionsHistoryRepository
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.utils.toPublicKey

class HistoryInteractor(
    private val rpcRepository: RpcRepository,
    private val historyTransactionsRepository: TransactionsHistoryRepository,
) {

    suspend fun getConfirmedTransaction(tokenPublicKey: String, transactionId: String): HistoryTransaction? {
        return historyTransactionsRepository.getTransactionsHistory(
            tokenPublicKey = tokenPublicKey,
            signatures = listOf(transactionId)
        )
            .firstOrNull()
    }

    suspend fun getHistory(tokenPublicKey: String, before: String?, limit: Int): List<HistoryTransaction> {
        val signatures = rpcRepository.getConfirmedSignaturesForAddress(
            userAccountAddress = tokenPublicKey.toPublicKey(),
            before = before,
            limit = limit
        )
            .map(SignatureInformationResponse::signature)

        return historyTransactionsRepository.getTransactionsHistory(tokenPublicKey, signatures)
    }
}
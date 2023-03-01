package org.p2p.ethereumkit.transactionsyncers

import org.p2p.ethereumkit.core.ITransactionProvider
import org.p2p.ethereumkit.core.ITransactionStorage
import org.p2p.ethereumkit.core.ITransactionSyncer
import org.p2p.ethereumkit.models.InternalTransaction
import org.p2p.ethereumkit.models.ProviderInternalTransaction
import org.p2p.ethereumkit.models.Transaction
import io.reactivex.Single

class InternalTransactionSyncer(
        private val transactionProvider: ITransactionProvider,
        private val storage: ITransactionStorage
) : ITransactionSyncer {

    private fun handle(transactions: List<ProviderInternalTransaction>) {
        if (transactions.isEmpty()) return

        val internalTransactions = transactions.map { tx ->
            InternalTransaction(tx.hash, tx.blockNumber, tx.from, tx.to, tx.value)
        }

        storage.saveInternalTransactions(internalTransactions)
    }

    override fun getTransactionsSingle(): Single<Pair<List<Transaction>, Boolean>> {
        val lastTransactionBlockNumber = storage.getLastInternalTransaction()?.blockNumber ?: 0
        val initial = lastTransactionBlockNumber == 0L

        return transactionProvider.getInternalTransactions(lastTransactionBlockNumber + 1)
                .doOnSuccess { providerInternalTransactions -> handle(providerInternalTransactions) }
                .map { providerInternalTransactions ->
                    val array = providerInternalTransactions.map { transaction ->
                        Transaction(
                                hash = transaction.hash,
                                timestamp = transaction.timestamp,
                                isFailed = false,
                                blockNumber = transaction.blockNumber,
                        )
                    }
                    Pair(array, initial)
                }
                .onErrorReturnItem(Pair(listOf(), initial))
    }

}

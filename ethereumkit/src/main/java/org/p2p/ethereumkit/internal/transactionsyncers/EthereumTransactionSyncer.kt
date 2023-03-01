package org.p2p.ethereumkit.internal.transactionsyncers

import org.p2p.ethereumkit.internal.core.ITransactionProvider
import org.p2p.ethereumkit.internal.core.ITransactionSyncer
import org.p2p.ethereumkit.internal.core.storage.TransactionSyncerStateStorage
import org.p2p.ethereumkit.internal.models.ProviderTransaction
import org.p2p.ethereumkit.internal.models.Transaction
import org.p2p.ethereumkit.internal.models.TransactionSyncerState
import io.reactivex.Single

class EthereumTransactionSyncer(
        private val transactionProvider: ITransactionProvider,
        private val storage: TransactionSyncerStateStorage
) : ITransactionSyncer {

    companion object {
        const val SyncerId = "ethereum-transaction-syncer"
    }

    override fun getTransactionsSingle(): Single<Pair<List<Transaction>, Boolean>> {
        val lastTransactionBlockNumber = storage.get(SyncerId)?.lastBlockNumber ?: 0
        val initial = lastTransactionBlockNumber == 0L

        return transactionProvider.getTransactions(lastTransactionBlockNumber + 1)
                .doOnSuccess { providerTransactions -> handle(providerTransactions) }
                .map { providerTransactions ->
                    val array = providerTransactions.map { transaction ->
                        val isFailed = when {
                            transaction.txReceiptStatus != null -> {
                                transaction.txReceiptStatus != 1
                            }
                            transaction.isError != null -> {
                                transaction.isError != 0
                            }
                            transaction.gasUsed != null -> {
                                transaction.gasUsed == transaction.gasLimit
                            }
                            else -> {
                                false
                            }
                        }

                        Transaction(
                                hash = transaction.hash,
                                timestamp = transaction.timestamp,
                                isFailed = isFailed,
                                blockNumber = transaction.blockNumber,
                                transactionIndex = transaction.transactionIndex,
                                from = transaction.from,
                                to = transaction.to,
                                value = transaction.value,
                                input = transaction.input,
                                nonce = transaction.nonce,
                                gasPrice = transaction.gasPrice,
                                gasUsed = transaction.gasUsed
                        )
                    }

                    Pair(array, initial)
                }
                .onErrorReturnItem(Pair(listOf(), initial))
    }

    private fun handle(transactions: List<ProviderTransaction>) {
        val maxBlockNumber = transactions.maxOfOrNull { it.blockNumber } ?: return
        val syncerState = TransactionSyncerState(SyncerId, maxBlockNumber)

        storage.save(syncerState)
    }

}

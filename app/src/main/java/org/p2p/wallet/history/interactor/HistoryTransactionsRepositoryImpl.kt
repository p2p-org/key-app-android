package org.p2p.wallet.history.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.wallet.history.db.dao.TransactionDaoDelegate
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.RpcRepository

class HistoryTransactionsRepositoryImpl(
    private val rpcRepository: RpcRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionDaoDelegate: TransactionDaoDelegate,
    private val transactionDetailsMapper: TransactionDetailsMapper,
    private val historyTransactionMapper: HistoryTransactionMapper
) : HistoryTransactionsRepository {
    override suspend fun getTransactionsHistory(
        tokenPublicKey: String,
        signatures: List<String>
    ): List<HistoryTransaction> {
        return withContext(Dispatchers.IO) {
            getTransactionsFromCache(signatures)
                .ifSizeNot(signatures.size) {
                    fetchTransactionsFromRemote(signatures)
                }
                .let { transactions ->
                    historyTransactionMapper.mapFromTransactionDetails(
                        transactions = transactions,
                        accountsInfo = getAccountsInfo(transactions),
                        userPublicKey = tokenKeyProvider.publicKey,
                        tokenPublicKey = tokenPublicKey
                    )
                }
                .sortedByDescending { it.date.toInstant().toEpochMilli() }
        }
    }

    private suspend fun getTransactionsFromCache(signatures: List<String>): List<TransactionDetails> {
        return transactionDaoDelegate.getTransactions(signatures)
            .let { transactionDetailsMapper.mapEntityToDomain(it) }
    }

    private suspend fun fetchTransactionsFromRemote(signatures: List<String>): List<TransactionDetails> {
        val confirmedTransactions = rpcRepository.getConfirmedTransactions(signatures)
        val transactionsFromRemote = transactionDetailsMapper.mapDtoToDomain(confirmedTransactions)

        transactionsFromRemote
            .let { transactionDetailsMapper.mapDomainToEntity(it) }
            .forEach { transactionDaoDelegate.insertTransaction(it) }

        return transactionsFromRemote
    }

    private suspend fun getAccountsInfo(
        fetchedTransactions: List<TransactionDetails>
    ): List<Pair<String, AccountInfo>> {
        // Making one request for all accounts info and caching values locally
        // to avoid multiple requests when constructing transaction
        val accountsInfoIds = fetchedTransactions
            .filterIsInstance<SwapDetails>()
            .flatMap { swapTransaction ->
                listOf(
                    swapTransaction.source,
                    swapTransaction.alternateSource,
                    swapTransaction.destination,
                    swapTransaction.alternateDestination
                )
            }
            .distinct()

        return if (accountsInfoIds.isNotEmpty()) {
            rpcRepository.getAccountsInfo(accountsInfoIds)
        } else {
            emptyList()
        }
    }

    private inline fun <E> List<E>.ifSizeNot(expectedSize: Int, defaultValue: () -> List<E>): List<E> {
        return if (this.size != expectedSize) defaultValue.invoke() else this
    }
}
package org.p2p.wallet.history.repository

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.wallet.history.db.dao.TransactionDaoDelegate
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.utils.ifSizeNot

class TransactionsHistoryRepository(
    private val rpcRepository: RpcRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionDaoDelegate: TransactionDaoDelegate,
    private val transactionDetailsMapper: TransactionDetailsEntityMapper,
    private val historyTransactionMapper: HistoryTransactionMapper,
    private val dispatchers: CoroutineDispatchers
) {
    suspend fun getTransactionsHistory(
        tokenPublicKey: String,
        signatures: List<String>
    ): List<HistoryTransaction> {
        return withContext(dispatchers.io) {
            getTransactions(signatures, tokenPublicKey)
        }
    }

    private fun getTransactions(signatures: List<String>, tokenPublicKey: String): List<HistoryTransaction> {
        return getTransactionsFromDatabase(signatures)
            .ifSizeNot(signatures.size) { getTransactionsFromNetwork(signatures) }
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

    private fun getTransactionsFromDatabase(signatures: List<String>): List<TransactionDetails> {
        return transactionDaoDelegate.getTransactions(signatures)
            .let { transactionDetailsMapper.mapEntityToDomain(it) }
    }

    private fun getTransactionsFromNetwork(signatures: List<String>): List<TransactionDetails> {
        val confirmedTransactions = runBlocking { rpcRepository.getConfirmedTransactions(signatures) }

        val transactionsToSave = confirmedTransactions
            .let { transactionDetailsMapper.mapDomainToEntity(it) }

        transactionDaoDelegate.insertTransactions(transactionsToSave)

        return confirmedTransactions
    }

    private fun getAccountsInfo(
        fetchedTransactions: List<TransactionDetails>
    ): List<Pair<String, AccountInfo>> {
        // Making one request for all accounts info and caching values locally
        // to avoid multiple requests when constructing transaction
        val accountsInfoIds = fetchedTransactions
            .filterIsInstance<SwapDetails>()
            .flatMap { swapTransaction ->
                setOfNotNull(
                    swapTransaction.source,
                    swapTransaction.alternateSource,
                    swapTransaction.destination,
                    swapTransaction.alternateDestination
                )
            }

        return if (accountsInfoIds.isNotEmpty()) {
            runBlocking { rpcRepository.getAccountsInfo(accountsInfoIds) }
        } else {
            emptyList()
        }
    }

    suspend fun deleteHistory() {
        withContext(dispatchers.io) {
            transactionDaoDelegate.deleteAll()
        }
    }
}
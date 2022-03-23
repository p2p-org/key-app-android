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
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.history.RpcHistoryRepository
import org.p2p.wallet.utils.ifSizeNot
import timber.log.Timber

class TransactionsHistoryRepository(
    private val rpcAccountRepository: RpcAccountRepository,
    private val rpcHistoryRepository: RpcHistoryRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val transactionDaoDelegate: TransactionDaoDelegate,
    private val transactionDetailsMapper: TransactionDetailsEntityMapper,
    private val historyTransactionMapper: HistoryTransactionMapper,
    private val dispatchers: CoroutineDispatchers
) {
    suspend fun getTransactionsHistory(
        tokenPublicKey: String,
        signatures: List<String>,
        forceRefresh: Boolean
    ): List<HistoryTransaction> {
        return withContext(dispatchers.io) {
            getTransactions(signatures, tokenPublicKey, forceRefresh)
        }
    }

    private fun getTransactions(
        confirmedSignatures: List<String>,
        tokenPublicKey: String,
        forceRefresh: Boolean,
    ): List<HistoryTransaction> {
        if (forceRefresh) {
            return getTransactionsFromNetwork(confirmedSignatures)
                .also(::saveTransactionsToDatabase)
                .mapToHistoryTransactions(tokenPublicKey)
        }

        return getTransactionsFromDatabase(confirmedSignatures)
            .also {
                if (it.isNotEmpty()) {
                    Timber.i("History Transactions are found in cache for token: $tokenPublicKey")
                }
            }
            .ifSizeNot(confirmedSignatures.size) {
                Timber.i(
                    "History Transactions are not cached fully for token $tokenPublicKey: " +
                        "expected=${confirmedSignatures.size} actual=${0}"
                )
                getTransactionsFromNetwork(confirmedSignatures)
            }
            .also(::saveTransactionsToDatabase)
            .mapToHistoryTransactions(tokenPublicKey)
    }

    private fun List<TransactionDetails>.mapToHistoryTransactions(tokenPublicKey: String): List<HistoryTransaction> {
        return historyTransactionMapper.mapTransactionDetailsToHistoryTransactions(
            transactions = this,
            accountsInfo = getAccountsInfo(this),
            userPublicKey = tokenKeyProvider.publicKey,
            tokenPublicKey = tokenPublicKey
        )
    }

    private fun getTransactionsFromDatabase(signatures: List<String>): List<TransactionDetails> {
        return transactionDaoDelegate.getTransactions(signatures)
            .let { transactionDetailsMapper.mapEntityToDomain(it) }
    }

    private fun getTransactionsFromNetwork(signatures: List<String>): List<TransactionDetails> {
        return runBlocking { rpcHistoryRepository.getConfirmedTransactions(signatures) }
    }

    private fun saveTransactionsToDatabase(transactionsFromRemote: List<TransactionDetails>) {
        val transactionsToSave = transactionsFromRemote
            .let { transactionDetailsMapper.mapDomainToEntity(it) }

        transactionDaoDelegate.insertTransactions(transactionsToSave)
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
            runBlocking { rpcAccountRepository.getAccountsInfo(accountsInfoIds) }
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

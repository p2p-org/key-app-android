package org.p2p.wallet.history.interactor

import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.solanaj.model.types.SignatureInformationResponse
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionMapper
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.history.repository.remote.TransactionDetailsRemoteRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRepository
import org.p2p.wallet.utils.ifNotEmpty
import org.p2p.wallet.utils.ifSizeNot
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber

class HistoryInteractor(
    private val rpcSignatureRepository: RpcSignatureRepository,
    private val rpcAccountRepository: RpcAccountRepository,
    private val transactionsRemoteRepository: TransactionDetailsRemoteRepository,
    private val transactionsLocalRepository: TransactionDetailsLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyTransactionMapper: HistoryTransactionMapper
) {

    suspend fun getHistoryTransaction(tokenPublicKey: String, transactionId: String): HistoryTransaction? {
        return getTransactionHistory(
            tokenPublicKey = tokenPublicKey,
            signatures = listOf(transactionId),
            forceNetwork = false
        )
            .firstOrNull()
    }

    suspend fun getAllHistoryTransactions(
        tokenPublicKey: String,
        before: String?,
        limit: Int,
        forceRefresh: Boolean
    ): List<HistoryTransaction> {
        val confirmedSignatures = rpcSignatureRepository.getConfirmedSignaturesForAddress(
            userAccountAddress = tokenPublicKey.toPublicKey(),
            before = before,
            limit = limit
        )
            .map(SignatureInformationResponse::signature)

        return getTransactionHistory(tokenPublicKey, confirmedSignatures, forceRefresh)
    }

    private suspend fun getTransactionHistory(
        tokenPublicKey: String,
        signatures: List<String>,
        forceNetwork: Boolean
    ): List<HistoryTransaction> {
        if (forceNetwork) {
            return transactionsRemoteRepository.getTransactions(signatures)
                .also { transactionsLocalRepository.saveTransactions(it) }
                .mapToHistoryTransactions(tokenPublicKey)
        }

        return transactionsLocalRepository.getTransactions(signatures)
            .ifNotEmpty { Timber.i("History Transactions are found in cache for token: $tokenPublicKey") }
            .ifSizeNot(signatures.size) {
                Timber.i(
                    "History Transactions are not cached fully for token $tokenPublicKey: " +
                        "expected=${signatures.size} actual=${it.size}"
                )
                transactionsRemoteRepository.getTransactions(signatures)
            }
            .also { transactionsLocalRepository.saveTransactions(it) }
            .mapToHistoryTransactions(tokenPublicKey)
    }

    private suspend fun List<TransactionDetails>.mapToHistoryTransactions(
        tokenPublicKey: String
    ): List<HistoryTransaction> {
        return historyTransactionMapper.mapTransactionDetailsToHistoryTransactions(
            transactions = this,
            accountsInfo = getAccountsInfo(this),
            userPublicKey = tokenKeyProvider.publicKey,
            tokenPublicKey = tokenPublicKey
        )
    }

    private suspend fun getAccountsInfo(
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
            rpcAccountRepository.getAccountsInfo(accountsInfoIds)
        } else {
            emptyList()
        }
    }
}

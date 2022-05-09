package org.p2p.wallet.history.interactor

import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionMapper
import org.p2p.wallet.history.model.RpcTransactionSignature
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.history.repository.remote.TransactionDetailsRemoteRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRepository
import org.p2p.wallet.utils.toPublicKey

private const val PAGE_LIMIT = 20

class HistoryInteractor(
    private val rpcSignatureRepository: RpcSignatureRepository,
    private val rpcAccountRepository: RpcAccountRepository,
    private val transactionsRemoteRepository: TransactionDetailsRemoteRepository,
    private val transactionsLocalRepository: TransactionDetailsLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyTransactionMapper: HistoryTransactionMapper
) {

    suspend fun loadSignatureForAddress(
        tokenPublicKey: String,
        before: String? = null
    ): List<RpcTransactionSignature> {

        val signatures = rpcSignatureRepository.getConfirmedSignaturesForAddress(
            userAccountAddress = tokenPublicKey.toPublicKey(),
            before = before,
            limit = PAGE_LIMIT
        )
        return signatures.map { RpcTransactionSignature(it.signature, it.confirmationStatus) }
    }

    suspend fun getHistoryTransaction(tokenPublicKey: String, transactionId: String) =
        transactionsLocalRepository.getTransactions(listOf(transactionId)).mapToHistoryTransactions(tokenPublicKey)
            .first()

    suspend fun loadTransactionHistory(
        tokenPublicKey: String,
        signaturesWithStatus: List<RpcTransactionSignature>,
        forceRefresh: Boolean
    ): List<HistoryTransaction> {
        if (forceRefresh) {
            transactionsLocalRepository.deleteAll()
        }
        val localTransactions = transactionsLocalRepository.getTransactions(signaturesWithStatus.map { it.signature })

        if (localTransactions.size != signaturesWithStatus.size) {
            val remoteTransaction = transactionsRemoteRepository.getTransactions(
                userPublicKey = tokenKeyProvider.publicKey,
                signatures = signaturesWithStatus
            )
            transactionsLocalRepository.saveTransactions(remoteTransaction)
            return remoteTransaction.mapToHistoryTransactions(tokenPublicKey)
        }
        return localTransactions.mapToHistoryTransactions(tokenPublicKey)
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

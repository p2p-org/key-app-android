package org.p2p.wallet.history.interactor

import kotlinx.coroutines.supervisorScope
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionMapper
import org.p2p.wallet.history.interactor.stream.AccountStreamSource
import org.p2p.wallet.history.interactor.stream.HistoryStreamSource
import org.p2p.wallet.history.interactor.stream.MultipleStreamSource
import org.p2p.wallet.history.interactor.stream.StreamSourceConfiguration
import org.p2p.wallet.history.model.RpcTransactionSignature
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.history.repository.remote.TransactionDetailsRemoteRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRepository
import org.p2p.wallet.user.interactor.UserInteractor

private const val DAY_IN_MILLISECONDS = 60 * 60 * 24
private const val PAGE_SIZE = 10

class HistoryInteractor(
    private val rpcAccountRepository: RpcAccountRepository,
    private val transactionsLocalRepository: TransactionDetailsLocalRepository,
    private val transactionsRemoteRepository: TransactionDetailsRemoteRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyTransactionMapper: HistoryTransactionMapper,
    private val rpcSignatureRepository: RpcSignatureRepository,
    private val userInteractor: UserInteractor
) {
    private val allSignatures = mutableListOf<RpcTransactionSignature>()
    private val tokenSignaturesMap = mutableMapOf<String, MutableList<RpcTransactionSignature>>()
    private lateinit var multipleStreamSource: HistoryStreamSource
    private val accountsStreamSources = mutableMapOf<String, HistoryStreamSource>()
    private val historyStreamSources = mutableListOf<HistoryStreamSource>()

    private suspend fun initStreamSources() {
        historyStreamSources.addAll(
            userInteractor.getUserTokens().map {
                val accountStreamSource = AccountStreamSource(it.publicKey, rpcSignatureRepository)
                accountsStreamSources[it.publicKey] = accountStreamSource
                accountStreamSource
            }
        )
        multipleStreamSource = MultipleStreamSource(historyStreamSources)
    }

    suspend fun loadTransactions(isRefresh: Boolean = false): List<HistoryTransaction> {
        if (historyStreamSources.isEmpty()) {
            initStreamSources()
        }
        if (isRefresh) {
            allSignatures.clear()
            tokenSignaturesMap.clear()
            multipleStreamSource.reset()
        }
        val signatures = loadAllSignatures()
        allSignatures.addAll(signatures)
        return loadTransactions(signatures)
    }

    suspend fun loadTransactions(account: String, isRefresh: Boolean = false): List<HistoryTransaction> {
        if (historyStreamSources.isEmpty()) {
            initStreamSources()
        }
        if (account !in tokenSignaturesMap) {
            tokenSignaturesMap[account] = mutableListOf()
        }
        if (isRefresh) {
            accountsStreamSources[account]?.reset()
            tokenSignaturesMap[account]?.clear()
        }

        val signatures = loadSignaturesForAccount(account, accountsStreamSources[account]!!)
        tokenSignaturesMap[account]?.addAll(signatures)
        return loadTransactions(signatures)
    }

    /*
    Idea of loading history filtered by timestamp
    1) First we load signatures of each token, and take first element, cause it's has latest timestamp
    2) We try to find the latest transaction signature from loaded transactions signatures
    3) Set period for transactions for one day, doing simple calculation
    dayPeriod = lastSignatureTime - DAY_IN_MILLISECONDS
    4) Loading signatures and filter them by this configuration
    5) Doing 4 step before we don't upload one page size of signatures
    6) Finally try to load transactions for this signatures
     */

    private suspend fun loadAllSignatures(): MutableList<RpcTransactionSignature> {
        val transactionsSignatures = mutableListOf<RpcTransactionSignature>()
        supervisorScope {
            while (true) {
                val firstItem = multipleStreamSource.currentItem() ?: break
                val time = (firstItem.streamSource?.blockTime ?: -1) - (DAY_IN_MILLISECONDS)

                while (true) {
                    val currentItem = multipleStreamSource.next(StreamSourceConfiguration(time))
                    if (!allSignatures.contains(currentItem?.streamSource)) {
                        transactionsSignatures.add(currentItem?.streamSource ?: break)
                    }
                    if (transactionsSignatures.size >= PAGE_SIZE) {
                        return@supervisorScope transactionsSignatures
                    }
                }
            }
        }
        return transactionsSignatures
    }

    private suspend fun loadSignaturesForAccount(
        account: String,
        accountStreamSource: HistoryStreamSource
    ): List<RpcTransactionSignature> {
        val transactionsSignatures = mutableListOf<RpcTransactionSignature>()

        val allTransactionSignatures = tokenSignaturesMap[account] ?: return emptyList()

        while (true) {
            val firstItem = accountStreamSource.currentItem() ?: break
            val time = (firstItem.streamSource?.blockTime ?: -1) - (DAY_IN_MILLISECONDS)

            while (true) {
                val currentItem = accountStreamSource.next(StreamSourceConfiguration(time))
                if (!allTransactionSignatures.contains(currentItem?.streamSource)) {
                    transactionsSignatures.add(currentItem?.streamSource ?: break)
                }
                if (transactionsSignatures.size >= 10) {
                    return transactionsSignatures
                }
            }
        }
        return transactionsSignatures
    }

    suspend fun getHistoryTransaction(tokenPublicKey: String, transactionId: String) =
        transactionsLocalRepository.getTransactions(listOf(transactionId))
            .mapToHistoryTransactions(tokenPublicKey)
            .first()

    private suspend fun loadTransactions(signatures: List<RpcTransactionSignature>): List<HistoryTransaction> {
        return transactionsRemoteRepository.getTransactions(tokenKeyProvider.publicKey, signatures)
            .mapToHistoryTransactions(tokenKeyProvider.publicKey)
    }

    suspend fun List<TransactionDetails>.mapToHistoryTransactions(
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

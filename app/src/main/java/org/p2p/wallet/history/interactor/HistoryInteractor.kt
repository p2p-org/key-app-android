package org.p2p.wallet.history.interactor

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
import timber.log.Timber

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
    private lateinit var multipleStreamSource: HistoryStreamSource
    private val accountsStreamSources = HashMap<String, HistoryStreamSource>()
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

    suspend fun loadTransactions(): List<HistoryTransaction> {
        if (historyStreamSources.isEmpty()) {
            initStreamSources()
        }
        val signatures = loadAllSignatures()
        allSignatures.addAll(signatures)
        return loadTransactions(signatures)
    }

    suspend fun loadTransactions(account: String): List<HistoryTransaction> {
        if (historyStreamSources.isEmpty()) {
            initStreamSources()
        }
        val signatures = loadSignaturesForAccount(account)
        return loadTransactions(signatures)
    }

    private suspend fun loadAllSignatures(): MutableList<RpcTransactionSignature> {
        val transactionsSignatures = mutableListOf<RpcTransactionSignature>()
        while (true) {
            val firstItem = multipleStreamSource.currentItem() ?: break
            val time = (firstItem.streamSource?.blockTime ?: -1) - (60 * 60 * 24)

            while (true) {
                val currentItem = multipleStreamSource.next(StreamSourceConfiguration(time))
                if (!allSignatures.contains(currentItem?.streamSource)) {
                    transactionsSignatures.add(currentItem?.streamSource ?: break)
                }
                if (transactionsSignatures.size >= 10) {
                    return transactionsSignatures
                }
            }
        }
        return transactionsSignatures
    }

    private suspend fun loadSignaturesForAccount(account: String): List<RpcTransactionSignature> {
        val transactionsSignatures = mutableListOf<RpcTransactionSignature>()
        val streamSource = accountsStreamSources[account]
        while (true) {
            val firstItem = streamSource?.currentItem() ?: break
            val time = (firstItem.streamSource?.blockTime ?: -1) - (60 * 60 * 24)

            while (true) {
                val currentItem = streamSource.next(StreamSourceConfiguration(time))
                if (!allSignatures.contains(currentItem?.streamSource)) {
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
        Timber.tag("TokenHistoryBuffer").d("AccountInfoSize = " + accountsInfoIds.size.toString())
        return if (accountsInfoIds.isNotEmpty()) {
            rpcAccountRepository.getAccountsInfo(accountsInfoIds)
        } else {
            emptyList()
        }
    }
}

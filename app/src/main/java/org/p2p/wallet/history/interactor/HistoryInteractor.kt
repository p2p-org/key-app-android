package org.p2p.wallet.history.interactor

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionMapper
import org.p2p.wallet.history.interactor.stream.AccountStreamSource
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

private const val TAG = "HistoryInteractor"

class HistoryInteractor(
    private val rpcAccountRepository: RpcAccountRepository,
    private val transactionsLocalRepository: TransactionDetailsLocalRepository,
    private val transactionsRemoteRepository: TransactionDetailsRemoteRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyTransactionMapper: HistoryTransactionMapper,
    private val rpcSignatureRepository: RpcSignatureRepository,
    private val userInteractor: UserInteractor
) {

    suspend fun loadTransactions(): List<HistoryTransaction> {
        val signatures = loadTransactionHistory()
        return loadTransactions(signatures)
    }

    private suspend fun loadTransactionHistory(): MutableList<RpcTransactionSignature> =
        withContext(
            Dispatchers.IO + CoroutineExceptionHandler { _, t ->
                Timber.tag(TAG).d("ERROR $t")
            }
        ) {
            val userAccounts = userInteractor.getUserTokens().map {
                AccountStreamSource(it.publicKey, it.tokenSymbol, transactionsRemoteRepository, rpcSignatureRepository)
            }
            val transactionsSignatures = mutableListOf<RpcTransactionSignature>()

            val multipleStreamSource = MultipleStreamSource(userAccounts)

            while (true) {
                val item = multipleStreamSource.currentItem() ?: break
                Timber.tag(TAG).d("Fetched new item = ${item.streamSource}")
                val time = (item.streamSource?.blockTime ?: -1) - (60 * 60 * 24)
                Timber.tag(TAG).d("Signature time = ${item.streamSource?.blockTime}")
                Timber.tag(TAG).d("RemainTime = ${(item.streamSource?.blockTime ?: -1) - (60 * 60 * 24)}")
                while (true) {
                    val item = multipleStreamSource.next(StreamSourceConfiguration(time))
                    Timber.tag(TAG).d("Found item for this time = $item")
                    transactionsSignatures.add(item?.streamSource ?: break)
                    if (transactionsSignatures.size >= 12) {
                        Timber.tag("______").d("new items is added, size = ${transactionsSignatures.size}")
                        return@withContext transactionsSignatures
                    }
                    Timber.tag(TAG).d("History size = ${transactionsSignatures.size}")
                }
            }
            return@withContext transactionsSignatures
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

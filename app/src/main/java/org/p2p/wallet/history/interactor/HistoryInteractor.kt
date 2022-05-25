package org.p2p.wallet.history.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionMapper
import org.p2p.wallet.history.interactor.buffer.HistoryTransactionsManager
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

class HistoryInteractor(
    private val rpcAccountRepository: RpcAccountRepository,
    private val transactionsLocalRepository: TransactionDetailsLocalRepository,
    private val transactionsRemoteRepository: TransactionDetailsRemoteRepository,
    private val signaturesRepository: RpcSignatureRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyTransactionMapper: HistoryTransactionMapper,
    private val userInteractor: UserInteractor,
    private val historyTransactionsManager: HistoryTransactionsManager
) {

    fun attachToHistoryFlow(): Flow<List<HistoryTransaction>> =
        historyTransactionsManager.getHistoryFlow()

    suspend fun loadTransactions() {
        Timber.tag("________").d("signatures started to load")
        val signatures = loadTransactionHistory()
        Timber.tag("________").d("signatures loaded = ${signatures.size}")
    }

    private suspend fun loadTransactionHistory(): MutableList<RpcTransactionSignature> {
        val userAccounts = userInteractor.getUserTokens().map {
            AccountStreamSource(it.publicKey, it.tokenSymbol, transactionsRemoteRepository, signaturesRepository)
        }
        val transactionsSignatures = mutableListOf<RpcTransactionSignature>()
        val multipleStreamSource = MultipleStreamSource(userAccounts)

        while (true) {
            val item = multipleStreamSource.currentItem() ?: return transactionsSignatures
            val time = (item.streamSource?.blockTime ?: -1) - (60 * 60 * 24)
            while (true) {
                val item = multipleStreamSource.next(StreamSourceConfiguration(time))
                transactionsSignatures.add(item?.streamSource ?: break)
                if (transactionsSignatures.size >= 15) {
                    return transactionsSignatures
                }
            }
        }
    }

    suspend fun getHistoryTransaction(tokenPublicKey: String, transactionId: String) =
        transactionsLocalRepository.getTransactions(listOf(transactionId))
            .mapToHistoryTransactions(tokenPublicKey)
            .first()

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

package org.p2p.wallet.history.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.wallet.common.di.ServiceScope
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionMapper
import org.p2p.wallet.history.interactor.buffer.TokensHistoryBuffer
import org.p2p.wallet.history.repository.local.TransactionDetailsLocalRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.user.interactor.UserInteractor

class HistoryInteractor(
    private val rpcAccountRepository: RpcAccountRepository,
    private val transactionsLocalRepository: TransactionDetailsLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyTransactionMapper: HistoryTransactionMapper,
    private val serviceScope: ServiceScope,
    private val userInteractor: UserInteractor,
    private val tokensHistoryBuffer: TokensHistoryBuffer
) {

    suspend fun attachToHistoryFlow(): Flow<List<HistoryTransaction>> =
        tokensHistoryBuffer.getHistoryFlow().map { it.mapToHistoryTransactions(tokenKeyProvider.publicKey) }

    suspend fun loadTransactionsHistory() = withContext(serviceScope.coroutineContext) {
        if (tokensHistoryBuffer.isEmpty()) {
            val userTokens = userInteractor.getUserTokens().map { it.publicKey }
            tokensHistoryBuffer.setup(userTokens)
        }
        tokensHistoryBuffer.load()
    }

    suspend fun getHistoryTransaction(tokenPublicKey: String, transactionId: String) =
        transactionsLocalRepository.getTransactions(listOf(transactionId))
            .mapToHistoryTransactions(tokenPublicKey)
            .first()

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

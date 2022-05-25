package org.p2p.wallet.history.interactor.buffer

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.supervisorScope
import org.p2p.solanaj.kits.transaction.SwapDetails
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.model.types.AccountInfo
import org.p2p.wallet.history.interactor.mapper.HistoryTransactionMapper
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.RpcTransactionSignature
import org.p2p.wallet.history.repository.remote.TransactionDetailsRemoteRepository
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.account.RpcAccountRepository
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRepository
import org.p2p.wallet.utils.toPublicKey
import java.util.concurrent.atomic.AtomicInteger

private const val TOKEN_PAGE_SIZE = 3

private const val TAG = "TokenHistoryBuffer"

class HistoryTransactionsManager(
    private val rpcSignatureRepository: RpcSignatureRepository,
    private val transactionRepository: TransactionDetailsRemoteRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val historyTransactionMapper: HistoryTransactionMapper,
    private val rpcAccountRepository: RpcAccountRepository
) : TransactionListener {

    private val tokensBuffer = mutableListOf<TokenHistoryBuffer>()
    private val transactionsFlow = MutableStateFlow<List<HistoryTransaction>>(mutableListOf())
    private val allTransactions = mutableListOf<HistoryTransaction>()

    private val sentRequestCount = AtomicInteger(0)
    private val receivedResponseCount = AtomicInteger(0)
    private var state = State.NONE

    fun isEmpty() = tokensBuffer.isEmpty()

    fun getHistoryFlow(): Flow<List<HistoryTransaction>> = transactionsFlow

    fun getState() = state

    fun setup(tokensBuffers: List<String>) {
        tokensBuffer.addAll(
            tokensBuffers.map {
                TokenHistoryBufferBuilder
                    .getInstance(it)
                    .setLastTransactionSignature(null)
                    .setBufferPayloadOffset(TOKEN_PAGE_SIZE)
                    .build()
            }
        )
        this.tokensBuffer.forEach { it.setListener(this) }
        state = State.IDLE
    }

    suspend fun load() {
        supervisorScope {
            state = State.LOADING
            tokensBuffer.forEach { buffer ->
                async(this.coroutineContext + NonCancellable) {

                    sentRequestCount.incrementAndGet()
                    val transactions = loadTransactions(buffer.getTokenAddress(), buffer.getLastSignature())
                    buffer.onTransactionsUploaded(transactions)
                }.invokeOnCompletion { throwable ->

                    if (throwable is EmptyDataException) {
                        sentRequestCount.decrementAndGet()
                        tokensBuffer.remove(buffer)
                        buffer.onFinish()
                    } else {
                        receivedResponseCount.incrementAndGet()
                    }
                    if (sentRequestCount.get() == receivedResponseCount.get()) {

                        transactionsFlow.value =
                            allTransactions.sortedByDescending { it.date.toInstant().toEpochMilli() }
                        sentRequestCount.set(0)
                        receivedResponseCount.set(0)
                        state = State.IDLE
                    }
                }
            }
        }
    }

    private suspend fun loadTransactions(
        tokenAddress: String,
        lastSignature: String?
    ): List<HistoryTransaction> {
        val signatures =
            rpcSignatureRepository.getConfirmedSignaturesForAddress(
                tokenAddress.toPublicKey(),
                lastSignature,
                TOKEN_PAGE_SIZE
            ).map { RpcTransactionSignature(it.signature, it.confirmationStatus, it.blockTime) }

        return transactionRepository.getTransactions(tokenKeyProvider.publicKey, signatures)
            .mapToHistoryTransactions(tokenKeyProvider.publicKey)
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

    override fun onTransactionsLoaded(items: List<HistoryTransaction>) {
        allTransactions.addAll(items.toList())
    }

    override fun onBufferFinished(tokenAddress: String) {
        tokensBuffer.remove(tokensBuffer.first { it.getTokenAddress() == tokenAddress })
    }

    enum class State {
        LOADING,
        IDLE,
        NONE
    }
}

package org.p2p.wallet.history.interactor.buffer

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.wallet.common.di.ServiceScope
import org.p2p.wallet.history.model.RpcTransactionSignature
import org.p2p.wallet.history.repository.remote.TransactionDetailsRemoteRepository
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRepository
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

private const val TOKEN_PAGE_SIZE = 5

private const val TAG = "TokenHistoryBuffer"

class TokensHistoryBuffer(
    private val rpcSignatureRepository: RpcSignatureRepository,
    private val transactionRepository: TransactionDetailsRemoteRepository,
    private val serviceScope: ServiceScope,
    private val tokenKeyProvider: TokenKeyProvider
) : TransactionListener {

    private val tokensBuffer = mutableListOf<TokenHistoryBuffer>()

    private val transactionsFlow = MutableStateFlow<List<TransactionDetails>>(emptyList())

    private val allTransactions = mutableListOf<TransactionDetails>()
    private val bufferedTransactions = mutableListOf<TransactionDetails>()

    private var sentRequestCount = AtomicInteger(0)
    private var receivedResponseCount = AtomicInteger(0)

    fun isEmpty() = tokensBuffer.isEmpty()

    fun getHistoryFlow(): Flow<List<TransactionDetails>> = transactionsFlow

    fun setup(tokensBuffers: List<String>) {
        tokensBuffer.addAll(
            tokensBuffers.map {
                TokenHistoryBufferBuilder
                    .getInstance(it)
                    .setLastTransactionSignature(null)
                    .setBufferPayloadOffset(20)
                    .build()
            }
        )
        this.tokensBuffer.forEach { it.setListener(this) }
    }

    suspend fun load() = withContext(serviceScope.coroutineContext) {
        tokensBuffer.forEach { buffer ->
            Timber.tag(TAG).d("Request transactions from buffer for ${buffer.getTokenAddress()}")
            when (val bufferState = buffer.requestTransactions(TOKEN_PAGE_SIZE)) {
                is BufferState.Load -> {
                    Timber.tag(TAG).d("Buffer is empty, items need to load = ${bufferState.itemsCountToLoad}")
                    async {
                        sentRequestCount.incrementAndGet()
                        val transactions = loadTransactions(buffer.getTokenAddress(), buffer.getLastSignature())
                        buffer.onTransactionsUploaded(transactions)
                    }.invokeOnCompletion {
                        if (it is EmptyDataException) {
                            sentRequestCount.decrementAndGet()
                            tokensBuffer.remove(buffer)
                        }
                    }
                }
                is BufferState.Idle -> {
                    Timber.tag(TAG)
                        .d(
                            "Buffer contains items, get from cache, " +
                                "${bufferState.items} items loaded, need load more =  ${bufferState.needsToLoadMore}"
                        )
                    async {
                        bufferedTransactions.addAll(bufferState.items)
                        if (bufferState.needsToLoadMore) {
                            val transactions = loadTransactions(buffer.getTokenAddress(), buffer.getLastSignature())
                            buffer.onTransactionsUploaded(transactions)
                        }
                    }
                }
            }
        }
        transactionsFlow.value = bufferedTransactions.toList()
    }

    private suspend fun loadTransactions(
        tokenAddress: String,
        lastSignature: String?
    ): List<TransactionDetails> = withContext(serviceScope.coroutineContext) {
        val signatures =
            rpcSignatureRepository.getConfirmedSignaturesForAddress(
                tokenAddress.toPublicKey(),
                lastSignature,
                TOKEN_PAGE_SIZE
            )
                .map { RpcTransactionSignature(it.signature, it.confirmationStatus, it.blockTime) }
                .sortedByDescending { it.blockTime }
        return@withContext transactionRepository.getTransactions(tokenKeyProvider.publicKey, signatures)
    }

    override fun onTransactionsLoaded(items: List<TransactionDetails>) {
        receivedResponseCount.incrementAndGet()
        allTransactions.addAll(items)
        Timber.tag(TAG)
            .d(
                "Received new transactions from server, and save them to buffer," +
                    " sent = $sentRequestCount, received = $receivedResponseCount"
            )
        if (sentRequestCount.get() == receivedResponseCount.get()) {
            Timber.tag(TAG).d("Fetched first pull of transactions, count = ${allTransactions.size}")
            transactionsFlow.value = allTransactions.toList()
            sentRequestCount = AtomicInteger(0)
            receivedResponseCount = AtomicInteger(0)
        }
    }
}

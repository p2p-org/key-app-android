package org.p2p.wallet.history.interactor.buffer

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.supervisorScope
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
    private val tokenKeyProvider: TokenKeyProvider,
) : TransactionListener {

    private val tokensBuffer = mutableListOf<TokenHistoryBuffer>()

    private val transactionsFlow = MutableStateFlow<MutableList<TransactionDetails>>(mutableListOf())

    private val allTransactions = mutableListOf<TransactionDetails>()

    private val sentRequestCount = AtomicInteger(0)
    private val receivedResponseCount = AtomicInteger(0)
    private var state = BufferManagerState.NONE

    fun isEmpty() = tokensBuffer.isEmpty()

    fun getHistoryFlow(): Flow<List<TransactionDetails>> = transactionsFlow

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
        state = BufferManagerState.IDLE
    }

    suspend fun load() {
        supervisorScope {
            state = BufferManagerState.LOADING
            Timber.tag(TAG)
                .d(
                    "sentRequest count ${sentRequestCount.get()}," +
                        " receivedRequestsCount = ${receivedResponseCount.get()}"
                )
            Timber.tag(TAG).d("Tokens left = ${tokensBuffer.size}")
            tokensBuffer.forEach { buffer ->
                Timber.tag(TAG).d("Request transactions from buffer for ${buffer.getTokenAddress()}")
                when (val bufferState = buffer.requestTransactions(TOKEN_PAGE_SIZE)) {
                    is BufferState.Load -> {
                        Timber.tag(TAG).d(
                            "Buffer is empty," +
                                " items need to load = ${bufferState.itemsCountToLoad}"
                        )
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
                            Timber.tag(TAG)
                                .d(
                                    "Received new transactions from server, and save them to buffer," +
                                        " sent = ${sentRequestCount.get()}, " +
                                        "received = ${receivedResponseCount.get()}, error = $throwable"
                                )
                            if (sentRequestCount.get() == receivedResponseCount.get()) {
                                Timber.tag(TAG).d(
                                    "Fetched first pull of transactions," +
                                        " count = ${allTransactions.size}"
                                )
                                transactionsFlow.value = allTransactions.toMutableList()
                                sentRequestCount.set(0)
                                receivedResponseCount.set(0)
                                state = BufferManagerState.IDLE
                                Timber.tag(TAG)
                                    .d(
                                        "sentRequest count ${sentRequestCount.get()}, " +
                                            "receivedRequestsCount = ${receivedResponseCount.get()}"
                                    )
                            }
                        }
                    }
                    is BufferState.Idle -> {
                        Timber.tag(TAG)
                            .d(
                                "Buffer contains items, get from cache, " +
                                    "$TOKEN_PAGE_SIZE items loaded, need load more =  ${bufferState.needsToLoadMore}"
                            )
                    }
                }
            }
        }
    }

    private suspend fun loadTransactions(
        tokenAddress: String,
        lastSignature: String?
    ): List<TransactionDetails> {
        val signatures =
            rpcSignatureRepository.getConfirmedSignaturesForAddress(
                tokenAddress.toPublicKey(),
                lastSignature,
                TOKEN_PAGE_SIZE
            ).map { RpcTransactionSignature(it.signature, it.confirmationStatus, it.blockTime) }
        return transactionRepository.getTransactions(tokenKeyProvider.publicKey, signatures)
    }

    override fun onTransactionsLoaded(items: List<TransactionDetails>) {
        Timber.tag(TAG).d("New items received = ${items.size}")
        allTransactions.addAll(items.toList())
    }

    override fun onBufferFinished(tokenAddress: String) {
        tokensBuffer.remove(tokensBuffer.first { it.getTokenAddress() == tokenAddress })
    }

    enum class BufferManagerState {
        LOADING,
        IDLE,
        NONE
    }
}

package org.p2p.wallet.history.interactor.stream

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.wallet.history.model.RpcTransactionSignature
import org.p2p.wallet.history.repository.remote.TransactionDetailsRemoteRepository
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRepository
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.util.concurrent.Executors

class AccountStreamSource(
    private val account: String,
    private val symbol: String,
    private val historyRepository: TransactionDetailsRemoteRepository,
    private val signatureRepository: RpcSignatureRepository
) : AbstractStreamSource() {

    private var lastFetchedSignature: String? = null
    private var batchSize = 15
    private var bufferSize = 15
    private val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()


    private val buffer = mutableListOf<RpcTransactionSignature>()

    override suspend fun next(configuration: StreamSourceConfiguration): HistoryStreamItem? {
        if (buffer.isEmpty()) {
            fillBuffer()
        }
        val signatureInfo = buffer.firstOrNull() ?: return null

        if (signatureInfo.blockTime >= configuration.timeStampEnd) {
            val signature = buffer.removeAt(0)
            return HistoryStreamItem(account, signature)
        }
        return null
    }

    override suspend fun currentItem(): HistoryStreamItem? {
        if (buffer.isEmpty()) {
            fillBuffer()
        }
        if (buffer.isEmpty()) {
            return null
        }
        return HistoryStreamItem(account, buffer.first())
    }

    override fun reset() {
    }

    private suspend fun fillBuffer() = withContext(executor) {
        async {
            val newSignatures = signatureRepository.getConfirmedSignaturesForAddress(
                account.toPublicKey(),
                lastFetchedSignature,
                batchSize
            ).map { RpcTransactionSignature(it.signature, it.confirmationStatus, it.blockTime) }
            lastFetchedSignature = newSignatures.lastOrNull()?.signature
            buffer.addAll(newSignatures)
        }.await()
    }
}

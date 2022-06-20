package org.p2p.wallet.history.interactor.stream

import org.p2p.wallet.history.model.RpcTransactionSignature
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRepository
import org.p2p.wallet.utils.toPublicKey

class AccountStreamSource(
    private val account: String,
    private val signatureRepository: RpcSignatureRepository
) : AbstractStreamSource() {

    private var lastFetchedSignature: String? = null
    private var batchSize = 15

    private var isPagingEnded = false
    private val buffer = mutableListOf<RpcTransactionSignature>()

    override suspend fun next(configuration: StreamSourceConfiguration): HistoryStreamItem? {
        try {
            if (buffer.isEmpty()) {
                fillBuffer()
            }
            val signatureInfo = buffer.firstOrNull() ?: return null

            if (signatureInfo.blockTime >= configuration.timeStampEnd) {
                val signature = buffer.removeAt(0)
                return HistoryStreamItem(account, signature)
            }
            return null
        } catch (e: EmptyDataException) {
            isPagingEnded = true
            return null
        }
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
        lastFetchedSignature = null
        buffer.clear()
    }

    private suspend fun fillBuffer() {
        try {
            if (isPagingEnded) return

            val signatures = signatureRepository.getConfirmedSignaturesForAddress(
                account.toPublicKey(),
                lastFetchedSignature,
                batchSize
            ).map { RpcTransactionSignature(it.signature, it.confirmationStatus, it.blockTime) }
            lastFetchedSignature = signatures.lastOrNull()?.signature
            buffer.addAll(signatures)
        } catch (e: EmptyDataException) {
            isPagingEnded = true
        }
    }
}

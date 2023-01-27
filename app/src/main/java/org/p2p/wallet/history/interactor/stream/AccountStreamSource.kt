package org.p2p.wallet.history.interactor.stream

import org.p2p.solanaj.model.types.SignatureInformationResponse
import org.p2p.wallet.history.model.RpcTransactionSignature
import org.p2p.wallet.infrastructure.network.data.EmptyDataException
import org.p2p.wallet.rpc.repository.signature.RpcSignatureRepository
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber

private const val TAG = "HistoryStreamSource"

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
            Timber.tag(TAG).i(e)
            return null
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
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
        isPagingEnded = false
        buffer.clear()
    }

    override fun isPagingReachedEnd(): Boolean {
        return isPagingEnded
    }

    private suspend fun fillBuffer() {
        try {
            if (isPagingEnded) return

            val signatures = signatureRepository.getConfirmedSignaturesForAddress(
                userAccountAddress = account.toPublicKey(),
                before = lastFetchedSignature,
                limit = batchSize
            )
                .map { it.toRpcSignature() }
            lastFetchedSignature = signatures.lastOrNull()?.signature
            buffer.addAll(signatures)
        } catch (e: EmptyDataException) {
            Timber.tag(TAG).i(e, "Failed to get next HistoryStreamItem because it is empty")
            isPagingEnded = true
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e)
        }
    }

    private fun SignatureInformationResponse.toRpcSignature(): RpcTransactionSignature =
        RpcTransactionSignature(
            signature = signature,
            status = confirmationStatus,
            blockTime = blockTime,
            error = transactionFailure
        )
}

package org.p2p.wallet.history.interactor.buffer

import org.p2p.wallet.history.model.HistoryTransaction
import timber.log.Timber

class TokenHistoryBuffer(
    private val tokenPublicKey: String,
    private var lastSignature: String?
) {

    private lateinit var transactionsLoadListener: TransactionListener

    private val tokenTransactions = mutableListOf<HistoryTransaction>()

    fun getLastSignature(): String? = lastSignature

    fun getTokenAddress(): String = tokenPublicKey

    fun onTransactionsUploaded(newItems: List<HistoryTransaction>) {
        tokenTransactions.addAll(newItems)
        this.lastSignature = newItems.lastOrNull()?.signature
        transactionsLoadListener.onTransactionsLoaded(newItems)
    }

    fun setListener(listener: TransactionListener) {
        this.transactionsLoadListener = listener
    }

    fun onFinish() {
        Timber.tag("TokenBuffer").d("Transactions loading finish for token address $tokenPublicKey")
    }
}

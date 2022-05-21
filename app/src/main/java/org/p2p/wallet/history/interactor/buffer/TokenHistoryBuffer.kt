package org.p2p.wallet.history.interactor.buffer

import org.p2p.solanaj.kits.transaction.TransactionDetails

class TokenHistoryBuffer(
    private val tokenPublicKey: String,
    private var lastSignature: String?,
    private val offset: Int
) {

    private lateinit var transactionsLoadListener: TransactionListener

    private val tokenTransactions = mutableListOf<TransactionDetails>()

    fun getLastSignature(): String? = lastSignature

    fun getTokenAddress(): String = tokenPublicKey

    fun requestTransactions(itemCount: Int): BufferState {
        if (tokenTransactions.isEmpty() || lastSignature == null) {
            // First state when we need load itemCount + offset
            return BufferState.Load(itemsCountToLoad = itemCount + offset)
        }
        val lastRequestedTransaction = tokenTransactions.first { it.signature == lastSignature }
        val indexOfLastRequestedTransaction = tokenTransactions.indexOf(lastRequestedTransaction)
        val cachedItemsSize = tokenTransactions.size - (indexOfLastRequestedTransaction + 1)

        return if (cachedItemsSize >= itemCount) {
            val cachedItems =
                tokenTransactions.subList(indexOfLastRequestedTransaction, indexOfLastRequestedTransaction + itemCount)
            lastSignature = cachedItems.lastOrNull()?.signature
            BufferState.Idle(cachedItems, needsToLoadMore = true)
        } else {
            BufferState.Load(itemsCountToLoad = offset)
        }
    }

    fun onTransactionsUploaded(newItems: List<TransactionDetails>) {
        tokenTransactions.addAll(newItems)
        lastSignature = newItems.lastOrNull()?.signature
        transactionsLoadListener.onTransactionsLoaded(newItems)
    }

    fun setListener(listener: TransactionListener) {
        this.transactionsLoadListener = listener
    }
}

sealed class BufferState {
    data class Idle(val items: List<TransactionDetails>, val needsToLoadMore: Boolean) : BufferState()
    data class Load(val itemsCountToLoad: Int) : BufferState()
}

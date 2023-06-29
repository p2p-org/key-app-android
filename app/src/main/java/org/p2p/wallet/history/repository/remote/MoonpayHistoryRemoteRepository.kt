package org.p2p.wallet.history.repository.remote

import org.p2p.core.utils.Constants
import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.infrastructure.sell.HiddenSellTransactionsStorageContract
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRemoteRepository
import org.p2p.core.crypto.toBase58Instance

class MoonpayHistoryRemoteRepository(
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val repository: MoonpaySellRemoteRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val hiddenSellTransactionsStorage: HiddenSellTransactionsStorageContract
) : HistoryRemoteRepository {

    private val cachedTransactions = mutableListOf<HistoryTransaction>()

    private var historyPagingState = HistoryPagingState.ACTIVE

    override fun getPagingState(mintAddress: String?): HistoryPagingState {
        return historyPagingState
    }

    override suspend fun loadHistory(limit: Int, mintAddress: String): HistoryPagingResult {
        val allHistoryOpened = mintAddress == Constants.WRAPPED_SOL_MINT
        if (!sellEnabledFeatureToggle.isFeatureEnabled || !allHistoryOpened) {
            historyPagingState = HistoryPagingState.INACTIVE
            return HistoryPagingResult.Success(emptyList())
        }

        cachedTransactions.clear()
        return try {
            val newTransactions = repository.getUserSellTransactions(
                userAddress = tokenKeyProvider.publicKey.toBase58Instance()
            )
            if (!cachedTransactions.containsAll(newTransactions)) {
                cachedTransactions.addAll(newTransactions)
            }
            historyPagingState = HistoryPagingState.INACTIVE
            val filteredSellTransactions = newTransactions.filterNot {
                hiddenSellTransactionsStorage.isTransactionHidden(it.transactionId)
            }
            HistoryPagingResult.Success(filteredSellTransactions)
        } catch (e: Throwable) {
            HistoryPagingResult.Error(e)
        }
    }

    override suspend fun loadNextPage(limit: Int, mintAddress: String): HistoryPagingResult {
        return HistoryPagingResult.Success(emptyList())
    }

    override suspend fun findTransactionById(id: String): HistoryTransaction? {
        return cachedTransactions.firstOrNull { it.getHistoryTransactionId() == id }
    }
}

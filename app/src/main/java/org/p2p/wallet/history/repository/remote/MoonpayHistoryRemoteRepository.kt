package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.common.feature_toggles.toggles.remote.SellEnabledFeatureToggle
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryPagingState
import org.p2p.wallet.history.model.HistoryTransaction

import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRemoteRepository
import org.p2p.wallet.utils.toBase58Instance

class MoonpayHistoryRemoteRepository(
    private val sellEnabledFeatureToggle: SellEnabledFeatureToggle,
    private val repository: MoonpaySellRemoteRepository,
    private val tokenKeyProvider: TokenKeyProvider
) : HistoryRemoteRepository {

    private val allTransactions = mutableListOf<HistoryTransaction>()

    private var historyPagingState = HistoryPagingState.ACTIVE

    override fun getPagingState(): HistoryPagingState {
        return historyPagingState
    }

    override suspend fun loadHistory(limit: Int, mintAddress: String?): HistoryPagingResult {
        if (!sellEnabledFeatureToggle.isFeatureEnabled || !mintAddress.isNullOrEmpty()) {
            historyPagingState = HistoryPagingState.INACTIVE
            return HistoryPagingResult.Success(emptyList())
        }
        allTransactions.clear()
        return try {
            val newTransactions = repository.getUserSellTransactions(
                userAddress = tokenKeyProvider.publicKey.toBase58Instance()
            )
            if (!allTransactions.containsAll(newTransactions)) {
                allTransactions.addAll(newTransactions)
            }
            historyPagingState = HistoryPagingState.INACTIVE
            HistoryPagingResult.Success(newTransactions)
        } catch (e: Throwable) {
            HistoryPagingResult.Error(e)
        }
    }

    override suspend fun loadNextPage(limit: Int, mintAddress: String?): HistoryPagingResult {
        return HistoryPagingResult.Success(emptyList())
    }

    override suspend fun findTransactionById(id: String): HistoryTransaction? {
        return allTransactions.firstOrNull { it.getHistoryTransactionId() == id }
    }
}
